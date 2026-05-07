package com.homi.service.service.finance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.finance.FinanceFlowSourceTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.common.lib.enums.finance.FinanceFlowTypeEnum;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.entity.OwnerPayableBillPayment;
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

    public List<FinanceFlow> getListBySource(String sourceType, Long sourceId) {
        return financeFlowRepo.getListBySource(sourceType, sourceId);
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

    public FinanceFlow createOwnerPayableBillPayFlow(OwnerPayableBillPayCommand command) {
        FinanceFlow financeFlow = new FinanceFlow();
        financeFlow.setFlowNo(generateFinanceFlowNo());
        financeFlow.setCompanyId(command.bill().getCompanyId());
        financeFlow.setSourceType(FinanceFlowSourceTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode());
        financeFlow.setSourceId(command.payment().getId());
        financeFlow.setSourceNo(command.payment().getPaymentNo());
        financeFlow.setBizType(FinanceBizTypeEnum.OWNER_PAYABLE_BILL_PAYMENT.getCode());
        financeFlow.setBizId(command.payment().getId());
        financeFlow.setBizNo(command.payment().getPaymentNo());
        financeFlow.setFlowType(FinanceFlowTypeEnum.PAY.getCode());
        financeFlow.setFlowDirection(FinanceFlowDirectionEnum.OUT.getCode());
        financeFlow.setAmount(defaultZero(command.payment().getPayAmount()).abs());
        financeFlow.setCurrency("CNY");
        financeFlow.setStatus(FinanceFlowStatusEnum.SUCCESS.getCode());
        financeFlow.setFlowAt(command.payment().getPayAt());
        financeFlow.setPayerName("平台");
        financeFlow.setReceiverName(command.ownerName());
        financeFlow.setOperatorId(command.operatorId());
        financeFlow.setOperatorName(command.operatorName());
        financeFlow.setRemark(command.remark());
        financeFlow.setExtJson(JSONUtil.createObj()
            .set("billId", command.bill().getId())
            .set("billNo", command.bill().getBillNo())
            .set("ownerId", command.bill().getOwnerId())
            .set("contractId", command.bill().getContractId())
            .toString());
        financeFlow.setCreateBy(command.operatorId());
        financeFlow.setCreateAt(command.now());
        financeFlow.setUpdateBy(command.operatorId());
        financeFlow.setUpdateAt(command.now());
        financeFlowRepo.save(financeFlow);
        return financeFlow;
    }

    private FinanceFlow buildFinanceFlow(CreateCommand command, LeaseBillFee fee, LeaseBillCollectDTO.Item item) {
        FinanceFlow financeFlow = new FinanceFlow();
        financeFlow.setFlowNo(generateFinanceFlowNo());
        financeFlow.setCompanyId(command.paymentFlow().getCompanyId());
        financeFlow.setSourceType(FinanceFlowSourceTypeEnum.PAYMENT_FLOW.getCode());
        financeFlow.setSourceId(command.paymentFlow().getId());
        financeFlow.setSourceNo(command.paymentFlow().getPaymentNo());
        financeFlow.setBizType(FinanceBizTypeEnum.LEASE_BILL_FEE.getCode());
        financeFlow.setBizId(item.getLeaseBillFeeId());
        financeFlow.setBizNo(String.valueOf(item.getLeaseBillFeeId()));
        BigDecimal amount = item.getAmount() == null ? BigDecimal.ZERO : item.getAmount();
        boolean expense = amount.compareTo(BigDecimal.ZERO) < 0;
        financeFlow.setFlowType(expense ? FinanceFlowTypeEnum.PAY.getCode() : FinanceFlowTypeEnum.RECEIVE.getCode());
        financeFlow.setFlowDirection(expense ? FinanceFlowDirectionEnum.OUT.getCode() : FinanceFlowDirectionEnum.IN.getCode());
        financeFlow.setAmount(amount.abs());
        financeFlow.setCurrency("CNY");
        financeFlow.setStatus(FinanceFlowStatusEnum.SUCCESS.getCode());
        financeFlow.setFlowAt(command.payAt());
        financeFlow.setPayerName(command.payerName());
        financeFlow.setPayerPhone(command.payerPhone());
        financeFlow.setOperatorId(command.operatorId());
        financeFlow.setOperatorName(command.operatorName());
        financeFlow.setRemark(command.remark());
        financeFlow.setCreateBy(command.operatorId());
        financeFlow.setCreateAt(command.now());
        financeFlow.setUpdateBy(command.operatorId());
        financeFlow.setUpdateAt(command.now());
        return financeFlow;
    }

    private String generateFinanceFlowNo() {
        return "FL" + IdUtil.getSnowflakeNextIdStr();
    }

    private BigDecimal defaultZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    @Builder
    public record CreateCommand(
        PaymentFlow paymentFlow,
        Map<Long, LeaseBillFee> feeMap,
        List<LeaseBillCollectDTO.Item> items,
        java.util.Date payAt,
        Long operatorId,
        String operatorName,
        String payerName,
        String payerPhone,
        String remark,
        DateTime now
    ) {
    }

    @Builder
    public record OwnerPayableBillPayCommand(
        OwnerPayableBill bill,
        OwnerPayableBillPayment payment,
        String ownerName,
        Long operatorId,
        String operatorName,
        String remark,
        DateTime now
    ) {
    }
}
