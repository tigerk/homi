package com.homi.service.service.owner;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillPaymentRecordStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillPaymentStatusEnum;
import com.homi.common.lib.enums.owner.OwnerPayableBillStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.entity.OwnerPayableBillFee;
import com.homi.model.dao.entity.OwnerPayableBillPayment;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.OwnerPayableBillFeeRepo;
import com.homi.model.dao.repo.OwnerPayableBillPaymentRepo;
import com.homi.model.dao.repo.OwnerPayableBillRepo;
import com.homi.model.dao.repo.OwnerRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.service.service.finance.FinanceFlowService;
import com.homi.service.service.finance.PaymentFlowService;
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
    private final OwnerPayableBillPaymentRepo ownerPayableBillPaymentRepo;
    private final OwnerRepo ownerRepo;
    private final UserRepo userRepo;
    private final FinanceFlowService financeFlowService;
    private final PaymentFlowService paymentFlowService;

    /**
     * 审批通过或无需审批时执行真实付款入账。
     */
    @Transactional(rollbackFor = Exception.class)
    public void completePayment(Long paymentId) {
        OwnerPayableBillPayment payment = ownerPayableBillPaymentRepo.getByIdForUpdate(paymentId);
        if (payment == null) {
            throw new BizException("包租应付付款记录不存在");
        }
        if (Objects.equals(payment.getPaymentStatus(), OwnerPayableBillPaymentRecordStatusEnum.SUCCESS.getCode())) {
            return;
        }
        if (Objects.equals(payment.getPaymentStatus(), OwnerPayableBillPaymentRecordStatusEnum.CLOSED.getCode())) {
            throw new BizException("已关闭付款记录不可入账");
        }

        OwnerPayableBill bill = ownerPayableBillRepo.getByIdForUpdate(payment.getBillId());
        if (bill == null) {
            throw new BizException("包租应付单不存在");
        }
        if (Objects.equals(bill.getBillStatus(), OwnerPayableBillStatusEnum.VOIDED.getCode())) {
            throw new BizException("已作废应付单不可付款");
        }
        if (defaultZero(payment.getPayAmount()).compareTo(defaultZero(bill.getUnpaidAmount())) > 0) {
            throw new BizException("付款金额不能超过未付金额");
        }
        List<OwnerPayableBillFee> feeList = ownerPayableBillFeeRepo.getByBillIdForUpdate(bill.getId());
        if (feeList.isEmpty()) {
            throw new BizException("包租应付单费用明细不存在");
        }

        DateTime now = DateUtil.date();
        String ownerName = resolveOwnerName(bill.getOwnerId());
        Long operatorId = payment.getCreateBy();
        String operatorName = userRepo.getUserNicknameById(operatorId);
        String remark = "包租应付单付款：" + bill.getBillNo();
        PaymentFlow paymentFlow = paymentFlowService.createOwnerPayableBillPaymentFlow(
            PaymentFlowService.CreateOwnerPayableBillPaymentCommand.builder()
                .bill(bill)
                .payment(payment)
                .ownerName(ownerName)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .remark(remark)
                .now(now)
                .build()
        );
        List<FinanceFlow> financeFlows = financeFlowService.createOwnerPayableBillPayFlows(
            FinanceFlowService.OwnerPayableBillPayCommand.builder()
                .bill(bill)
                .payment(payment)
                .feeList(feeList)
                .paymentFlowId(paymentFlow.getId())
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

        BigDecimal paidAmount = defaultZero(bill.getPaidAmount()).add(defaultZero(payment.getPayAmount()));
        bill.setPaidAmount(paidAmount);
        bill.setUnpaidAmount(defaultZero(bill.getPayableAmount()).subtract(paidAmount).max(BigDecimal.ZERO));
        bill.setPaymentStatus(resolvePaymentStatus(bill));
        bill.setUpdateBy(payment.getCreateBy());
        bill.setUpdateAt(now);
        ownerPayableBillRepo.updateById(bill);

        payment.setPaymentStatus(OwnerPayableBillPaymentRecordStatusEnum.SUCCESS.getCode());
        payment.setApprovalStatus(BizApprovalStatusEnum.APPROVED.getCode());
        payment.setFinanceFlowId(financeFlows.get(0).getId());
        payment.setUpdateBy(payment.getCreateBy());
        payment.setUpdateAt(now);
        ownerPayableBillPaymentRepo.updateById(payment);
    }

    /**
     * 审批驳回或撤回时关闭付款申请，不影响原账单修改或作废。
     */
    @Transactional(rollbackFor = Exception.class)
    public void closePayment(Long paymentId, Integer approvalStatus) {
        OwnerPayableBillPayment payment = ownerPayableBillPaymentRepo.getByIdForUpdate(paymentId);
        if (payment == null
            || Objects.equals(payment.getPaymentStatus(), OwnerPayableBillPaymentRecordStatusEnum.SUCCESS.getCode())
            || Objects.equals(payment.getPaymentStatus(), OwnerPayableBillPaymentRecordStatusEnum.CLOSED.getCode())) {
            return;
        }

        DateTime now = DateUtil.date();
        payment.setPaymentStatus(OwnerPayableBillPaymentRecordStatusEnum.CLOSED.getCode());
        payment.setApprovalStatus(approvalStatus);
        payment.setUpdateBy(payment.getCreateBy());
        payment.setUpdateAt(now);
        ownerPayableBillPaymentRepo.updateById(payment);
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
