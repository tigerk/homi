package com.homi.service.service.finance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowChannelEnum;
import com.homi.common.lib.enums.finance.PaymentFlowDirectionEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.FinanceFlowRepo;
import com.homi.model.dao.repo.LeaseBillRepo;
import com.homi.model.dao.repo.PaymentFlowRepo;
import com.homi.service.service.lease.bill.component.LeaseBillUpdater;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentFlowService {
    private final PaymentFlowRepo paymentFlowRepo;
    private final FinanceFlowRepo financeFlowRepo;
    private final LeaseBillRepo leaseBillRepo;
    private final LeaseBillUpdater leaseBillUpdater;

    public PaymentFlow getLatestByBiz(String bizType, Long bizId) {
        return paymentFlowRepo.getByBiz(bizType, bizId);
    }

    public List<PaymentFlow> listByBiz(String bizType, Long bizId) {
        return paymentFlowRepo.listByBiz(bizType, bizId);
    }

    public PaymentFlow getById(Long id) {
        return id == null ? null : paymentFlowRepo.getById(id);
    }

    public PaymentFlow createLeaseBillPaymentFlow(CreateCommand command) {
        LeaseBill bill = command.bill();
        PaymentFlow paymentFlow = new PaymentFlow();
        paymentFlow.setPaymentNo(generatePaymentNo());
        paymentFlow.setCompanyId(bill.getCompanyId());
        paymentFlow.setBizType(PaymentFlowBizTypeEnum.LEASE_BILL.getCode());
        paymentFlow.setBizId(bill.getId());
        paymentFlow.setChannel(resolvePaymentChannel(command.payChannel()));
        paymentFlow.setThirdTradeNo(command.thirdTradeNo());
        paymentFlow.setPaymentVoucherUrl(command.paymentVoucherUrl());
        paymentFlow.setAmount(command.totalAmount());
        paymentFlow.setCurrency("CNY");
        paymentFlow.setRefundedAmount(BigDecimal.ZERO);
        paymentFlow.setFlowDirection(PaymentFlowDirectionEnum.IN.getCode());
        paymentFlow.setStatus(command.status());
        paymentFlow.setApprovalStatus(command.approvalStatus());
        paymentFlow.setPayAt(command.payAt());
        paymentFlow.setPayerName(command.payerName());
        paymentFlow.setPayerPhone(command.payerPhone());
        paymentFlow.setOperatorId(command.operatorId());
        paymentFlow.setOperatorName(command.operatorName());
        paymentFlow.setRemark(command.remark());
        paymentFlow.setExtJson(command.extJson());
        paymentFlow.setCreateBy(command.operatorId());
        paymentFlow.setCreateAt(command.now());
        paymentFlow.setUpdateBy(command.operatorId());
        paymentFlow.setUpdateAt(command.now());
        paymentFlowRepo.save(paymentFlow);
        return paymentFlow;
    }

    public void updateStatus(Long paymentFlowId, Integer status, Long operatorId, DateTime now) {
        PaymentFlow paymentFlow = paymentFlowRepo.getById(paymentFlowId);
        if (paymentFlow == null) {
            return;
        }
        paymentFlow.setStatus(status);
        paymentFlow.setUpdateBy(operatorId);
        paymentFlow.setUpdateAt(now);
        paymentFlowRepo.updateById(paymentFlow);
    }

    public void updateApprovalStatus(Long paymentFlowId, Integer approvalStatus, Long operatorId, DateTime now) {
        PaymentFlow paymentFlow = paymentFlowRepo.getById(paymentFlowId);
        if (paymentFlow == null) {
            return;
        }
        paymentFlow.setApprovalStatus(approvalStatus);
        paymentFlow.setUpdateBy(operatorId);
        paymentFlow.setUpdateAt(now);
        paymentFlowRepo.updateById(paymentFlow);
    }

    public void updateApprovalAndStatus(Long paymentFlowId, Integer approvalStatus, Integer status, Long operatorId, DateTime now) {
        PaymentFlow paymentFlow = paymentFlowRepo.getById(paymentFlowId);
        if (paymentFlow == null) {
            return;
        }
        paymentFlow.setApprovalStatus(approvalStatus);
        paymentFlow.setStatus(status);
        paymentFlow.setUpdateBy(operatorId);
        paymentFlow.setUpdateAt(now);
        paymentFlowRepo.updateById(paymentFlow);
    }

    @Transactional(rollbackFor = Exception.class)
    public void voidLeaseBillPaymentFlow(Long paymentFlowId, String voidReason, Long operatorId) {
        PaymentFlow paymentFlow = paymentFlowRepo.getByIdForUpdate(paymentFlowId);
        if (paymentFlow == null) {
            throw new BizException("支付流水不存在");
        }
        if (!PaymentFlowBizTypeEnum.LEASE_BILL.getCode().equals(paymentFlow.getBizType())) {
            throw new BizException("仅租客账单支付流水允许作废");
        }
        if (PaymentFlowStatusEnum.VOIDED.getCode().equals(paymentFlow.getStatus())) {
            throw new BizException("支付流水已作废");
        }
        if (!PaymentFlowStatusEnum.SUCCESS.getCode().equals(paymentFlow.getStatus())) {
            throw new BizException("仅支付成功流水允许作废");
        }

        LeaseBill bill = leaseBillRepo.getByIdForUpdate(paymentFlow.getBizId());
        if (bill == null) {
            throw new BizException("关联租客账单不存在");
        }

        DateTime now = cn.hutool.core.date.DateUtil.date();
        String finalReason = CharSequenceUtil.blankToDefault(CharSequenceUtil.trim(voidReason), "支付流水作废");

        paymentFlow.setStatus(PaymentFlowStatusEnum.VOIDED.getCode());
        paymentFlow.setRemark(appendVoidReason(paymentFlow.getRemark(), finalReason));
        paymentFlow.setUpdateBy(operatorId);
        paymentFlow.setUpdateAt(now);
        paymentFlowRepo.updateById(paymentFlow);

        List<FinanceFlow> financeFlows = financeFlowRepo.getListByPaymentFlowIdForUpdate(paymentFlowId);
        if (!financeFlows.isEmpty()) {
            for (FinanceFlow financeFlow : financeFlows) {
                financeFlow.setStatus(FinanceFlowStatusEnum.VOIDED.getCode());
                financeFlow.setRemark(appendVoidReason(financeFlow.getRemark(), finalReason));
                financeFlow.setUpdateBy(operatorId);
                financeFlow.setUpdateAt(now);
            }
            financeFlowRepo.updateBatchById(financeFlows);
        }

        leaseBillUpdater.recalculateFromFinanceFlows(bill, operatorId, now);
    }

    private String appendVoidReason(String remark, String voidReason) {
        String suffix = "【作废原因】" + voidReason;
        if (CharSequenceUtil.isBlank(remark)) {
            return suffix;
        }
        if (remark.contains(suffix)) {
            return remark;
        }
        return remark + " " + suffix;
    }

    private String generatePaymentNo() {
        return "PAY" + IdUtil.getSnowflakeNextIdStr();
    }

    private String resolvePaymentChannel(Integer payChannel) {
        if (payChannel == null) {
            return PaymentFlowChannelEnum.OTHER.getCode();
        }
        return switch (payChannel) {
            case 1 -> PaymentFlowChannelEnum.CASH.getCode();
            case 2 -> PaymentFlowChannelEnum.TRANSFER.getCode();
            case 3 -> PaymentFlowChannelEnum.ALIPAY.getCode();
            case 4 -> PaymentFlowChannelEnum.WECHAT.getCode();
            default -> PaymentFlowChannelEnum.OTHER.getCode();
        };
    }

    @Builder
    public record CreateCommand(
        LeaseBill bill,
        BigDecimal totalAmount,
        Integer payChannel,
        String thirdTradeNo,
        String paymentVoucherUrl,
        Date payAt,
        Long operatorId,
        String operatorName,
        String payerName,
        String payerPhone,
        String remark,
        Integer status,
        Integer approvalStatus,
        String extJson,
        DateTime now
    ) {
    }
}
