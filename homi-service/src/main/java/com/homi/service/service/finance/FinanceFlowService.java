package com.homi.service.service.finance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.common.lib.enums.finance.FinanceFlowTypeEnum;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.LeaseBill;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.FinanceFlowRepo;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceFlowService {
    private final FinanceFlowRepo financeFlowRepo;

    public List<FinanceFlow> getListByBiz(String bizType, Long bizId) {
        return financeFlowRepo.getListByBiz(bizType, bizId);
    }

    public void createLeaseBillReceiveFlows(CreateCommand command) {
        if (command.feeList() == null || command.feeList().isEmpty()) {
            financeFlowRepo.save(buildFinanceFlow(command, null, command.amount()));
            return;
        }

        List<FinanceFlow> financeFlows = command.feeList().stream()
            .map(fee -> buildFinanceFlow(command, fee, fee.getAmount()))
            .toList();
        financeFlowRepo.saveBatch(financeFlows);
    }

    private FinanceFlow buildFinanceFlow(CreateCommand command, LeaseBillFee fee, BigDecimal amount) {
        LeaseBill bill = command.bill();
        FinanceFlow financeFlow = new FinanceFlow();
        financeFlow.setFlowNo(generateFinanceFlowNo());
        financeFlow.setCompanyId(bill.getCompanyId());
        financeFlow.setPaymentFlowId(command.paymentFlow().getId());
        financeFlow.setBizType(FinanceBizTypeEnum.LEASE_BILL.getCode());
        financeFlow.setBizId(bill.getId());
        financeFlow.setBizNo(String.valueOf(bill.getId()));
        financeFlow.setFlowType(FinanceFlowTypeEnum.RECEIVE.getCode());
        financeFlow.setFlowDirection(FinanceFlowDirectionEnum.IN.getCode());
        financeFlow.setAmount(toCent(amount));
        financeFlow.setCurrency("CNY");
        financeFlow.setStatus(FinanceFlowStatusEnum.SUCCESS.getCode());
        financeFlow.setFlowTime(command.payTime());
        financeFlow.setPayerName(command.payerName());
        financeFlow.setPayerPhone(command.payerPhone());
        financeFlow.setOperatorId(command.operatorId());
        financeFlow.setOperatorName(command.operatorName());
        financeFlow.setFeeType(fee != null ? fee.getFeeType() : null);
        financeFlow.setFeeRefId(fee != null ? fee.getId() : null);
        financeFlow.setFeeName(fee != null ? fee.getName() : command.remark());
        financeFlow.setRemark(fee != null ? fee.getRemark() : bill.getRemark());
        financeFlow.setCreateBy(command.operatorId());
        financeFlow.setCreateTime(command.now());
        financeFlow.setUpdateBy(command.operatorId());
        financeFlow.setUpdateTime(command.now());
        return financeFlow;
    }

    private String generateFinanceFlowNo() {
        return "FL" + IdUtil.getSnowflakeNextIdStr();
    }

    private Long toCent(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
    }

    @Builder
    public record CreateCommand(
        LeaseBill bill,
        PaymentFlow paymentFlow,
        List<LeaseBillFee> feeList,
        BigDecimal amount,
        java.util.Date payTime,
        Long operatorId,
        String operatorName,
        String payerName,
        String payerPhone,
        String remark,
        DateTime now
    ) {
    }
}
