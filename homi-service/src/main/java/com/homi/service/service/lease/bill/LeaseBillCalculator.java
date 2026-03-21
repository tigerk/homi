package com.homi.service.service.lease.bill;

import cn.hutool.core.util.ObjectUtil;
import com.homi.common.lib.enums.pay.PayStatusEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 账单纯计算工具组件。
 *
 * <p>本类只做数据计算与校验，不注入任何其他 Service，
 * 可被 {@link LeaseBillService} 和 {@link PaymentApprovalService} 同时注入，
 * 从根本上消除两者之间的循环依赖。
 *
 * <p>放入此处的方法须满足：<b>无副作用、不操作数据库、不依赖其他 Bean</b>。
 */
@Component
public class LeaseBillCalculator {

    /**
     * 根据已收金额与总金额推导支付状态。
     *
     * @param paidAmount  已收金额
     * @param totalAmount 应收总金额
     * @return {@link PayStatusEnum} 对应 code
     */
    public Integer resolvePayStatus(BigDecimal paidAmount, BigDecimal totalAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return PayStatusEnum.UNPAID.getCode();
        }
        if (totalAmount != null && paidAmount.compareTo(totalAmount) >= 0) {
            return PayStatusEnum.PAID.getCode();
        }
        return PayStatusEnum.PARTIALLY_PAID.getCode();
    }

    /**
     * 校验本次收款分摊是否合法。
     *
     * <p>规则：
     * <ul>
     *   <li>每笔分摊金额必须 &gt; 0 且不超过费用项未收金额。</li>
     *   <li>费用项 ID 不能重复。</li>
     *   <li>费用项必须归属于当前账单。</li>
     *   <li>所有分摊之和必须等于本次收款总额。</li>
     * </ul>
     *
     * @param dto    收款请求
     * @param bill   账单实体（用于校验费用项归属）
     * @param feeMap 涉及的费用项映射（已加行锁）
     * @return 校验通过返回 {@code true}
     */
    public boolean validateCollectItems(LeaseBillCollectDTO dto,
                                        LeaseBill bill,
                                        Map<Long, LeaseBillFee> feeMap) {
        BigDecimal totalAmount = ObjectUtil.defaultIfNull(dto.getTotalAmount(), BigDecimal.ZERO);
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        Set<Long> duplicateGuard = new HashSet<>();

        for (LeaseBillCollectDTO.Item item : dto.getItems()) {
            if (item == null || item.getLeaseBillFeeId() == null
                || !duplicateGuard.add(item.getLeaseBillFeeId())) {
                return true;
            }
            LeaseBillFee fee = feeMap.get(item.getLeaseBillFeeId());
            BigDecimal amount = ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO);
            if (!isCollectableItem(bill, fee, amount)) {
                return true;
            }
            allocatedAmount = allocatedAmount.add(amount);
        }
        return allocatedAmount.compareTo(totalAmount) != 0;
    }

    /**
     * 汇总费用项应收总金额。
     */
    public BigDecimal sumAmount(List<LeaseBillFee> fees) {
        return fees.stream()
            .map(f -> ObjectUtil.defaultIfNull(f.getAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 汇总费用项已收总金额。
     */
    public BigDecimal sumPaidAmount(List<LeaseBillFee> fees) {
        return fees.stream()
            .map(f -> ObjectUtil.defaultIfNull(f.getPaidAmount(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 构建账单摘要描述，用于支付流水备注等场景。
     */
    public String buildBillSummary(LeaseBill bill) {
        if (bill == null) {
            return "租客账单收款";
        }
        return "租客账单#" + bill.getId();
    }

    // -------------------------------------------------------------------------
    // 私有
    // -------------------------------------------------------------------------

    /**
     * 判断费用项是否可以被本次收款分摊。
     * 条件：归属账单正确、分摊金额 &gt; 0 且不超过未收金额。
     */
    private boolean isCollectableItem(LeaseBill bill, LeaseBillFee fee, BigDecimal amount) {
        return fee != null
            && Objects.equals(fee.getBillId(), bill.getId())
            && amount.compareTo(BigDecimal.ZERO) > 0
            && amount.compareTo(ObjectUtil.defaultIfNull(fee.getUnpaidAmount(), BigDecimal.ZERO)) <= 0;
    }
}
