package com.homi.service.service.tenant;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.repo.LeaseBillFeeRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.service.service.finance.FinanceFlowService;
import com.homi.service.service.finance.PaymentFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentApprovalService {
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillFeeRepo leaseBillFeeRepo;
    private final TenantRepo tenantRepo;
    private final UserRepo userRepo;
    private final FinanceFlowService financeFlowService;
    private final PaymentFlowService paymentFlowService;
    private final LeaseBillPayerResolver leaseBillPayerResolver;

    /**
     * 审批通过后完成一次账单收款入账。
     * <p>
     * 该方法负责回放 payment_flow.ext_json 中暂存的分摊明细，
     * 生成财务流水并回写账单费用项与账单汇总状态。
     */
    @Transactional(rollbackFor = Exception.class)
    public void completePaymentFlowCollection(Long paymentFlowId) {
        PaymentFlow paymentFlow = paymentFlowService.getById(paymentFlowId);
        if (paymentFlow == null || !Objects.equals(paymentFlow.getStatus(), PaymentFlowStatusEnum.PENDING_APPROVAL.getCode())) {
            return;
        }

        LeaseBillCollectDTO dto = JSONUtil.toBean(paymentFlow.getExtJson(), LeaseBillCollectDTO.class);
        if (dto == null || dto.getId() == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            paymentFlowService.updateStatus(paymentFlow.getId(), PaymentFlowStatusEnum.FAILED.getCode(), paymentFlow.getUpdateBy(), DateUtil.date());
            return;
        }

        LeaseBill bill = leaseBillRepo.getByIdForUpdate(dto.getId());
        if (bill == null) {
            paymentFlowService.updateStatus(paymentFlow.getId(), PaymentFlowStatusEnum.FAILED.getCode(), dto.getUpdateBy(), DateUtil.date());
            return;
        }

        List<Long> feeIds = dto.getItems().stream()
            .map(LeaseBillCollectDTO.Item::getLeaseBillFeeId)
            .filter(Objects::nonNull)
            .toList();
        Map<Long, LeaseBillFee> feeMap = leaseBillFeeRepo.getByIdsForUpdate(feeIds).stream()
            .collect(Collectors.toMap(LeaseBillFee::getId, item -> item));
        if (!validateCollectItems(dto, bill, feeMap)) {
            paymentFlowService.updateStatus(paymentFlow.getId(), PaymentFlowStatusEnum.FAILED.getCode(), dto.getUpdateBy(), DateUtil.date());
            return;
        }

        DateTime now = DateUtil.date();
        Tenant tenant = tenantRepo.getById(bill.getTenantId());
        LeaseBillPayerResolver.BillPayerInfo payerInfo = leaseBillPayerResolver.resolve(tenant);
        String operatorName = userRepo.getUserNicknameById(dto.getUpdateBy());
        Date payTime = dto.getPayTime() != null ? dto.getPayTime() : paymentFlow.getPayTime();

        financeFlowService.createLeaseBillReceiveFlows(
            FinanceFlowService.CreateCommand.builder()
                .paymentFlow(paymentFlow)
                .feeMap(feeMap)
                .items(dto.getItems())
                .payTime(payTime)
                .operatorId(dto.getUpdateBy())
                .operatorName(operatorName)
                .payerName(payerInfo.payerName())
                .payerPhone(payerInfo.payerPhone())
                .remark(buildBillSummary(bill))
                .now(now)
                .build()
        );

        applyCollectToFees(feeMap, dto.getItems(), dto.getUpdateBy(), now);
        recalculateBillAmounts(bill, dto.getUpdateBy(), now);
        paymentFlowService.updateApprovalAndStatus(paymentFlow.getId(),
            BizApprovalStatusEnum.APPROVED.getCode(),
            PaymentFlowStatusEnum.SUCCESS.getCode(),
            dto.getUpdateBy(),
            now);
    }

    /**
     * 审批驳回或撤回后关闭支付流水，不做账单入账。
     */
    @Transactional(rollbackFor = Exception.class)
    public void closePaymentFlowCollection(Long paymentFlowId, Integer approvalStatus) {
        PaymentFlow paymentFlow = paymentFlowService.getById(paymentFlowId);
        if (paymentFlow == null || !Objects.equals(paymentFlow.getStatus(), PaymentFlowStatusEnum.PENDING_APPROVAL.getCode())) {
            return;
        }
        paymentFlowService.updateApprovalAndStatus(paymentFlow.getId(),
            approvalStatus,
            PaymentFlowStatusEnum.CLOSED.getCode(),
            paymentFlow.getUpdateBy(),
            DateUtil.date());
    }

    private boolean validateCollectItems(LeaseBillCollectDTO dto, LeaseBill bill, Map<Long, LeaseBillFee> feeMap) {
        BigDecimal totalAmount = ObjectUtil.defaultIfNull(dto.getTotalAmount(), BigDecimal.ZERO);
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        Set<Long> duplicateGuard = new HashSet<>();
        for (LeaseBillCollectDTO.Item item : dto.getItems()) {
            if (item == null || item.getLeaseBillFeeId() == null) {
                return false;
            }
            if (!duplicateGuard.add(item.getLeaseBillFeeId())) {
                return false;
            }
            LeaseBillFee fee = feeMap.get(item.getLeaseBillFeeId());
            if (fee == null || !Objects.equals(fee.getBillId(), bill.getId())) {
                return false;
            }
            BigDecimal amount = ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO);
            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(ObjectUtil.defaultIfNull(fee.getUnpaidAmount(), BigDecimal.ZERO)) > 0) {
                return false;
            }
            allocatedAmount = allocatedAmount.add(amount);
        }
        return allocatedAmount.compareTo(totalAmount) == 0;
    }

    private void applyCollectToFees(Map<Long, LeaseBillFee> feeMap, List<LeaseBillCollectDTO.Item> items, Long operatorId, DateTime now) {
        for (LeaseBillCollectDTO.Item item : items) {
            LeaseBillFee fee = feeMap.get(item.getLeaseBillFeeId());
            BigDecimal nextPaidAmount = ObjectUtil.defaultIfNull(fee.getPaidAmount(), BigDecimal.ZERO)
                .add(ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO));
            BigDecimal totalAmount = ObjectUtil.defaultIfNull(fee.getAmount(), BigDecimal.ZERO);
            if (nextPaidAmount.compareTo(totalAmount) > 0) {
                nextPaidAmount = totalAmount;
            }
            fee.setPaidAmount(nextPaidAmount);
            fee.setUnpaidAmount(totalAmount.subtract(nextPaidAmount));
            fee.setPayStatus(resolvePayStatus(nextPaidAmount, totalAmount));
            fee.setUpdateBy(operatorId);
            fee.setUpdateTime(now);
        }
        leaseBillFeeRepo.updateBatchById(feeMap.values().stream().toList());
    }

    private void recalculateBillAmounts(LeaseBill bill, Long operatorId, DateTime now) {
        List<LeaseBillFee> allFees = leaseBillFeeRepo.getFeesByBillIdForUpdate(bill.getId());
        BigDecimal billTotalAmount = allFees.stream()
            .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal billPaidAmount = allFees.stream()
            .map(item -> ObjectUtil.defaultIfNull(item.getPaidAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setTotalAmount(billTotalAmount);
        bill.setPaidAmount(billPaidAmount);
        bill.setUnpaidAmount(billTotalAmount.subtract(billPaidAmount));
        bill.setPayStatus(resolvePayStatus(billPaidAmount, billTotalAmount));
        bill.setUpdateBy(operatorId);
        bill.setUpdateTime(now);
        leaseBillRepo.updateById(bill);
    }

    private Integer resolvePayStatus(BigDecimal paidAmount, BigDecimal totalAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return PayStatusEnum.UNPAID.getCode();
        }
        if (totalAmount != null && paidAmount.compareTo(totalAmount) >= 0) {
            return PayStatusEnum.PAID.getCode();
        }
        return PayStatusEnum.PARTIALLY_PAID.getCode();
    }

    private String buildBillSummary(LeaseBill bill) {
        if (bill == null) {
            return "租客账单收款";
        }
        return "租客账单#" + bill.getId();
    }

}
