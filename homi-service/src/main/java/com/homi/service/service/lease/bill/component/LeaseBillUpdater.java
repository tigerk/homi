package com.homi.service.service.lease.bill.component;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.repo.FinanceFlowRepo;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.service.service.lease.bill.PaymentApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账单更新组件。
 * <p>
 * 职责单一：基于费用项重算账单的 totalAmount / paidAmount /
 * unpaidAmount / payStatus 并持久化。
 * <p>
 * LeaseBillService 和 PaymentApprovalService 都依赖此组件，
 * 两者之间不再互相注入，循环依赖彻底消除。
 */
@Component
@RequiredArgsConstructor
public class LeaseBillUpdater {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final FinanceFlowRepo financeFlowRepo;
    private final LeaseBillCalculator billCalculator;

    /**
     * 基于账单下全部费用项重算账单汇总金额与支付状态，并持久化。
     *
     * <p>由 {@link PaymentApprovalService} 在审批通过入账后调用。
     *
     * @param bill       账单实体（会被直接修改后 update）
     * @param operatorId 操作人 ID
     * @param now        操作时间
     */
    public void recalculate(LeaseBill bill, Long operatorId, DateTime now) {
        List<LeaseBillFee> allFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());

        BigDecimal totalAmount = billCalculator.sumAmount(allFees);
        BigDecimal paidAmount = billCalculator.sumPaidAmount(allFees);

        bill.setTotalAmount(totalAmount);
        bill.setPaidAmount(paidAmount);
        bill.setUnpaidAmount(totalAmount.subtract(paidAmount));
        bill.setPayStatus(billCalculator.resolvePayStatus(paidAmount, totalAmount));
        bill.setUpdateBy(operatorId);
        bill.setUpdateAt(now);
        leaseBillRepo.updateById(bill);
    }

    /**
     * 基于当前仍然有效的财务流水，重算账单费用项已收/待收和账单支付状态。
     */
    public void recalculateFromFinanceFlows(LeaseBill bill, Long operatorId, DateTime now) {
        List<LeaseBillFee> allFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());
        if (allFees.isEmpty()) {
            recalculate(bill, operatorId, now);
            return;
        }

        List<Long> feeIds = allFees.stream().map(LeaseBillFee::getId).toList();
        Map<Long, BigDecimal> paidAmountMap = financeFlowRepo.getListByBizIds(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode(), feeIds).stream()
            .filter(item -> item.getBizId() != null)
            .filter(item -> FinanceFlowStatusEnum.SUCCESS.getCode().equals(item.getStatus()))
            .collect(java.util.stream.Collectors.groupingBy(
                FinanceFlow::getBizId,
                java.util.stream.Collectors.mapping(
                    item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO),
                    java.util.stream.Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));

        for (LeaseBillFee fee : allFees) {
            BigDecimal totalAmount = ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO);
            BigDecimal paidAmount = ObjectUtil.defaultIfNull(paidAmountMap.get(fee.getId()), BigDecimal.ZERO);
            if (paidAmount.compareTo(totalAmount) > 0) {
                paidAmount = totalAmount;
            }
            fee.setPaidAmount(paidAmount);
            fee.setUnpaidAmount(totalAmount.subtract(paidAmount));
            fee.setPayStatus(billCalculator.resolvePayStatus(paidAmount, totalAmount));
            fee.setUpdateBy(operatorId);
            fee.setUpdateAt(now);
        }
        leaseBillFeeRepo.updateBatchById(allFees);
        recalculate(bill, operatorId, now);
    }

    /**
     * 将本次收款金额写入各费用项，更新已收 / 未收 / 支付状态。
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/23 10:19
     *
     * @param feeMap     参数说明
     * @param items      参数说明
     * @param operatorId 参数说明
     * @param now        参数说明
     */
    public void applyCollectToFees(Map<Long, LeaseBillFee> feeMap, List<LeaseBillCollectDTO.Item> items, Long operatorId, DateTime now) {
        for (LeaseBillCollectDTO.Item item : items) {
            LeaseBillFee fee = feeMap.get(item.getLeaseBillFeeId());
            BigDecimal totalAmount = ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO);
            BigDecimal nextPaidAmount = ObjectUtil.defaultIfNull(fee.getPaidAmount(), BigDecimal.ZERO)
                .add(ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO));
            // 防止浮点误差导致超收
            if (nextPaidAmount.compareTo(totalAmount) > 0) {
                nextPaidAmount = totalAmount;
            }
            fee.setPaidAmount(nextPaidAmount);
            fee.setUnpaidAmount(totalAmount.subtract(nextPaidAmount));
            fee.setPayStatus(billCalculator.resolvePayStatus(nextPaidAmount, totalAmount));
            fee.setUpdateBy(operatorId);
            fee.setUpdateAt(now);
        }
        leaseBillFeeRepo.updateBatchById(feeMap.values().stream().toList());
    }

    /**
     * 应用费用项变更, 包括删除、更新、创建费用项。
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/23 10:23
     *
     * @param command 参数说明
     */
    public void applyFeeChanges(LeaseBillUpdater.FeeSyncCommand command) {
        if (!command.removedIds().isEmpty()) {
            leaseBillFeeRepo.removeByIds(command.removedIds());
        }
        if (!command.toUpdate().isEmpty()) {
            leaseBillFeeRepo.updateBatchById(command.toUpdate());
        }
        if (!command.toCreate().isEmpty()) {
            leaseBillFeeRepo.saveBatch(command.toCreate());
        }
    }

    // -------------------------------------------------------------------------
    // 内部记录类
    // -------------------------------------------------------------------------

    /**
     * 费用项同步命令（新增 / 更新 / 删除三类操作的容器）。
     */
    public record FeeSyncCommand(List<Long> removedIds,
                                  List<LeaseBillFee> toCreate,
                                  List<LeaseBillFee> toUpdate) {
    }
}
