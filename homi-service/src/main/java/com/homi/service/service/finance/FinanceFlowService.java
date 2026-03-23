package com.homi.service.service.finance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.common.lib.enums.finance.FinanceFlowTypeEnum;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.FinanceFlowRepo;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceFlowService {
    private final FinanceFlowRepo financeFlowRepo;

    public List<FinanceFlow> getListByBiz(String bizType, Long bizId) {
        return financeFlowRepo.getListByBiz(bizType, bizId);
    }

    public List<FinanceFlow> getListByBizIds(String bizType, List<Long> bizIds) {
        return financeFlowRepo.getListByBizIds(bizType, bizIds);
    }

    public List<FinanceFlow> getListByPaymentFlowId(Long paymentFlowId) {
        return financeFlowRepo.getListByPaymentFlowId(paymentFlowId);
    }

    public boolean existsByBizIds(String bizType, List<Long> bizIds) {
        return financeFlowRepo.existsByBizIds(bizType, bizIds);
    }

    public void createLeaseBillReceiveFlows(CreateCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            return;
        }

        List<FinanceFlow> financeFlows = command.items().stream()
            .map(item -> buildFinanceFlow(command, command.feeMap().get(item.getLeaseBillFeeId()), item))
            .toList();
        financeFlowRepo.saveBatch(financeFlows);
    }

    private FinanceFlow buildFinanceFlow(CreateCommand command, LeaseBillFee fee, LeaseBillCollectDTO.Item item) {
        FinanceFlow financeFlow = new FinanceFlow();
        financeFlow.setFlowNo(generateFinanceFlowNo());
        financeFlow.setCompanyId(command.paymentFlow().getCompanyId());
        financeFlow.setPaymentFlowId(command.paymentFlow().getId());
        financeFlow.setBizType(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode());
        financeFlow.setBizId(item.getLeaseBillFeeId());
        financeFlow.setBizNo(String.valueOf(item.getLeaseBillFeeId()));
        financeFlow.setFlowType(FinanceFlowTypeEnum.RECEIVE.getCode());
        financeFlow.setFlowDirection(FinanceFlowDirectionEnum.IN.getCode());
        financeFlow.setAmount(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
        financeFlow.setCurrency("CNY");
        financeFlow.setStatus(FinanceFlowStatusEnum.SUCCESS.getCode());
        financeFlow.setFlowTime(command.payTime());
        financeFlow.setPayerName(command.payerName());
        financeFlow.setPayerPhone(command.payerPhone());
        financeFlow.setOperatorId(command.operatorId());
        financeFlow.setOperatorName(command.operatorName());
        financeFlow.setRemark(command.remark());
        financeFlow.setCreateBy(command.operatorId());
        financeFlow.setCreateTime(command.now());
        financeFlow.setUpdateBy(command.operatorId());
        financeFlow.setUpdateTime(command.now());
        return financeFlow;
    }

    private String generateFinanceFlowNo() {
        return "FL" + IdUtil.getSnowflakeNextIdStr();
    }

    @Builder
    public record CreateCommand(
        PaymentFlow paymentFlow,
        Map<Long, LeaseBillFee> feeMap,
        List<LeaseBillCollectDTO.Item> items,
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
