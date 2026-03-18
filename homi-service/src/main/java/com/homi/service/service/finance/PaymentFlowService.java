package com.homi.service.service.finance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.homi.common.lib.enums.finance.PaymentFlowBizTypeEnum;
import com.homi.common.lib.enums.finance.PaymentFlowChannelEnum;
import com.homi.common.lib.enums.finance.PaymentFlowDirectionEnum;
import com.homi.common.lib.enums.finance.PaymentFlowStatusEnum;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.PaymentFlowRepo;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentFlowService {
    private final PaymentFlowRepo paymentFlowRepo;

    public PaymentFlow getLatestByBiz(String bizType, Long bizId) {
        return paymentFlowRepo.getByBiz(bizType, bizId);
    }

    public List<PaymentFlow> listByBiz(String bizType, Long bizId) {
        return paymentFlowRepo.listByBiz(bizType, bizId);
    }

    public PaymentFlow createLeaseBillPaymentFlow(CreateCommand command) {
        LeaseBill bill = command.bill();
        PaymentFlow paymentFlow = new PaymentFlow();
        paymentFlow.setPaymentNo(generatePaymentNo());
        paymentFlow.setCompanyId(bill.getCompanyId());
        paymentFlow.setBizType(PaymentFlowBizTypeEnum.LEASE_BILL.getCode());
        paymentFlow.setBizId(bill.getId());
        paymentFlow.setChannel(resolvePaymentChannel(command.payChannel()));
        paymentFlow.setAmount(toCent(command.totalAmount()));
        paymentFlow.setCurrency("CNY");
        paymentFlow.setRefundedAmount(0L);
        paymentFlow.setFlowDirection(PaymentFlowDirectionEnum.IN.getCode());
        paymentFlow.setStatus(PaymentFlowStatusEnum.SUCCESS.getCode());
        paymentFlow.setPayTime(command.payTime());
        paymentFlow.setPayerName(command.payerName());
        paymentFlow.setPayerPhone(command.payerPhone());
        paymentFlow.setOperatorId(command.operatorId());
        paymentFlow.setOperatorName(command.operatorName());
        paymentFlow.setRemark(command.remark());
        paymentFlow.setCreateBy(command.operatorId());
        paymentFlow.setCreateTime(command.now());
        paymentFlow.setUpdateBy(command.operatorId());
        paymentFlow.setUpdateTime(command.now());
        paymentFlowRepo.save(paymentFlow);
        return paymentFlow;
    }

    private String generatePaymentNo() {
        return "PAY" + IdUtil.getSnowflakeNextIdStr();
    }

    private Long toCent(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
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
        Date payTime,
        Long operatorId,
        String operatorName,
        String payerName,
        String payerPhone,
        String remark,
        DateTime now
    ) {
    }
}
