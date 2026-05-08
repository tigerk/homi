package com.homi.service.service.owner;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillPaymentStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.entity.OwnerPayableBillFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.OwnerPayableBillFeeRepo;
import com.homi.model.dao.repo.OwnerPayableBillRepo;
import com.homi.model.dao.repo.OwnerRepo;
import com.homi.model.dao.repo.PaymentFlowRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.service.service.finance.FinanceFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OwnerPayableBillPaymentApprovalService {
    private final OwnerPayableBillRepo ownerPayableBillRepo;
    private final OwnerPayableBillFeeRepo ownerPayableBillFeeRepo;
    private final PaymentFlowRepo paymentFlowRepo;
    private final OwnerRepo ownerRepo;
    private final UserRepo userRepo;
    private final FinanceFlowService financeFlowService;

    /**
     * 审批通过或无需审批时执行真实付款入账。
     */
    @Transactional(rollbackFor = Exception.class)
    public void completePayment(Long paymentFlowId) {
        PaymentFlow paymentFlow = paymentFlowRepo.getByIdForUpdate(paymentFlowId);
        if (paymentFlow == null) {
            throw new BizException("包租应付支付流水不存在");
        }
        if (!Objects.equals(paymentFlow.getBizType(), PaymentFlowBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode())) {
            throw new BizException("支付流水业务类型不匹配");
        }
        if (Objects.equals(paymentFlow.getStatus(), PaymentFlowStatusEnum.SUCCESS.getCode())) {
            return;
        }
        if (Objects.equals(paymentFlow.getStatus(), PaymentFlowStatusEnum.CLOSED.getCode())) {
            throw new BizException("已关闭支付流水不可入账");
        }

        OwnerPayableBill bill = ownerPayableBillRepo.getByIdForUpdate(paymentFlow.getBizId());
        if (bill == null) {
            throw new BizException("包租应付单不存在");
        }
        if (Objects.equals(bill.getBillStatus(), OwnerPayableBillStatusEnum.VOIDED.getCode())) {
            throw new BizException("已作废应付单不可付款");
        }
        if (defaultZero(paymentFlow.getAmount()).compareTo(defaultZero(bill.getUnpaidAmount())) > 0) {
            throw new BizException("付款金额不能超过未付金额");
        }
        List<OwnerPayableBillFee> feeList = ownerPayableBillFeeRepo.getByBillIdForUpdate(bill.getId());
        if (feeList.isEmpty()) {
            throw new BizException("包租应付单费用明细不存在");
        }

        DateTime now = DateUtil.date();
        String ownerName = resolveOwnerName(bill.getOwnerId());
        Long operatorId = paymentFlow.getCreateBy();
        String operatorName = userRepo.getUserNicknameById(operatorId);
        String remark = "包租应付单付款：" + bill.getBillNo();
        List<FinanceFlow> financeFlows = financeFlowService.createOwnerPayableBillPayFlows(
            FinanceFlowService.OwnerPayableBillPayCommand.builder()
                .bill(bill)
                .paymentFlow(paymentFlow)
                .feeList(feeList)
                .ownerName(ownerName)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .remark(remark)
                .now(now)
                .build()
        );
        if (financeFlows.isEmpty()) {
            throw new BizException("包租应付付款财务流水生成失败");
        }

        BigDecimal paidAmount = defaultZero(bill.getPaidAmount()).add(defaultZero(paymentFlow.getAmount()));
        bill.setPaidAmount(paidAmount);
        bill.setUnpaidAmount(defaultZero(bill.getPayableAmount()).subtract(paidAmount).max(BigDecimal.ZERO));
        bill.setPaymentStatus(resolvePaymentStatus(bill));
        bill.setUpdateBy(operatorId);
        bill.setUpdateAt(now);
        ownerPayableBillRepo.updateById(bill);

        paymentFlow.setStatus(PaymentFlowStatusEnum.SUCCESS.getCode());
        paymentFlow.setApprovalStatus(BizApprovalStatusEnum.APPROVED.getCode());
        paymentFlow.setUpdateBy(operatorId);
        paymentFlow.setUpdateAt(now);
        paymentFlowRepo.updateById(paymentFlow);
    }

    /**
     * 审批驳回或撤回时关闭付款申请，不影响原账单修改或作废。
     */
    @Transactional(rollbackFor = Exception.class)
    public void closePayment(Long paymentFlowId, Integer approvalStatus) {
        PaymentFlow paymentFlow = paymentFlowRepo.getByIdForUpdate(paymentFlowId);
        if (paymentFlow == null
            || Objects.equals(paymentFlow.getStatus(), PaymentFlowStatusEnum.SUCCESS.getCode())
            || Objects.equals(paymentFlow.getStatus(), PaymentFlowStatusEnum.CLOSED.getCode())) {
            return;
        }

        DateTime now = DateUtil.date();
        paymentFlow.setStatus(PaymentFlowStatusEnum.CLOSED.getCode());
        paymentFlow.setApprovalStatus(approvalStatus);
        paymentFlow.setUpdateBy(paymentFlow.getCreateBy());
        paymentFlow.setUpdateAt(now);
        paymentFlowRepo.updateById(paymentFlow);
    }

    private Integer resolvePaymentStatus(OwnerPayableBill bill) {
        if (defaultZero(bill.getUnpaidAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return OwnerPayableBillPaymentStatusEnum.PAID.getCode();
        }
        if (defaultZero(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0) {
            return OwnerPayableBillPaymentStatusEnum.PART_PAID.getCode();
        }
        return OwnerPayableBillPaymentStatusEnum.UNPAID.getCode();
    }

    private String resolveOwnerName(Long ownerId) {
        if (ownerId == null) {
            return null;
        }
        Owner owner = ownerRepo.getById(ownerId);
        return owner == null ? null : owner.getOwnerName();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
