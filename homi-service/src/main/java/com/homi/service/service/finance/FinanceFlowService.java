package com.homi.service.service.finance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.finance.FinanceBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.finance.FinanceFlowStatusEnum;
import com.homi.common.lib.enums.finance.FinanceFlowTypeEnum;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.entity.LeaseBillFee;
import com.homi.model.dao.entity.OwnerPayableBill;
import com.homi.model.dao.entity.OwnerPayableBillFee;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.repo.FinanceFlowRepo;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<FinanceFlow> getListByPaymentFlowIds(List<Long> paymentFlowIds) {
        return financeFlowRepo.getListByPaymentFlowIds(paymentFlowIds);
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

    public List<FinanceFlow> createOwnerPayableBillPayFlows(OwnerPayableBillPayCommand command) {
        if (command.feeList() == null || command.feeList().isEmpty()) {
            return List.of();
        }
        List<OwnerPayableBillFee> feeList = command.feeList().stream()
            .filter(Objects::nonNull)
            .filter(item -> item.getId() != null)
            .sorted(Comparator.comparing(OwnerPayableBillFee::getId))
            .toList();
        if (feeList.isEmpty()) {
            return List.of();
        }

        List<OwnerPayableBillFeeRemaining> remainingList = buildOwnerPayableBillFeeRemainingList(command, feeList);
        BigDecimal paymentAmount = defaultZero(command.paymentFlow().getAmount()).abs();
        List<OwnerPayableBillFeeAllocation> allocations = allocateOwnerPayableBillFeeAmount(remainingList, paymentAmount);
        if (allocations.isEmpty()) {
            return List.of();
        }

        List<FinanceFlow> financeFlows = allocations.stream()
            .map(item -> buildOwnerPayableBillPayFlow(command, item))
            .toList();
        financeFlowRepo.saveBatch(financeFlows);
        return financeFlows;
    }

    public FinanceFlow createOwnerPayableBillPayFlow(OwnerPayableBillPayCommand command) {
        List<FinanceFlow> financeFlows = createOwnerPayableBillPayFlows(command);
        return financeFlows.isEmpty() ? null : financeFlows.get(0);
    }

    private FinanceFlow buildOwnerPayableBillPayFlow(OwnerPayableBillPayCommand command, OwnerPayableBillFeeAllocation allocation) {
        OwnerPayableBillFee fee = allocation.fee();
        BigDecimal signedAmount = allocation.signedAmount();
        boolean outgoing = signedAmount.compareTo(BigDecimal.ZERO) >= 0;
        FinanceFlow financeFlow = new FinanceFlow();
        financeFlow.setFlowNo(generateFinanceFlowNo());
        financeFlow.setCompanyId(command.bill().getCompanyId());
        financeFlow.setPaymentFlowId(command.paymentFlow().getId());
        financeFlow.setBizType(FinanceBizTypeEnum.OWNER_PAYABLE_BILL_FEE.getCode());
        financeFlow.setBizId(fee.getId());
        financeFlow.setBizNo(String.valueOf(fee.getId()));
        financeFlow.setFlowType(outgoing ? FinanceFlowTypeEnum.PAY.getCode() : FinanceFlowTypeEnum.RECEIVE.getCode());
        financeFlow.setFlowDirection(outgoing ? FinanceFlowDirectionEnum.OUT.getCode() : FinanceFlowDirectionEnum.IN.getCode());
        financeFlow.setAmount(signedAmount.abs());
        financeFlow.setCurrency("CNY");
        financeFlow.setStatus(FinanceFlowStatusEnum.SUCCESS.getCode());
        financeFlow.setFlowAt(command.paymentFlow().getPayAt());
        financeFlow.setPayerName(outgoing ? "平台" : command.ownerName());
        financeFlow.setReceiverName(outgoing ? command.ownerName() : "平台");
        financeFlow.setOperatorId(command.operatorId());
        financeFlow.setOperatorName(command.operatorName());
        financeFlow.setRemark(command.remark());
        financeFlow.setExtJson(JSONUtil.createObj()
            .set("billId", command.bill().getId())
            .set("billNo", command.bill().getBillNo())
            .set("ownerId", command.bill().getOwnerId())
            .set("contractId", command.bill().getContractId())
            .set("paymentFlowId", command.paymentFlow().getId())
            .set("paymentNo", command.paymentFlow().getPaymentNo())
            .set("feeId", fee.getId())
            .set("feeType", fee.getFeeType())
            .set("feeName", fee.getFeeName())
            .set("feeDirection", fee.getDirection())
            .toString());
        financeFlow.setCreateBy(command.operatorId());
        financeFlow.setCreateAt(command.now());
        financeFlow.setUpdateBy(command.operatorId());
        financeFlow.setUpdateAt(command.now());
        return financeFlow;
    }

    private List<OwnerPayableBillFeeRemaining> buildOwnerPayableBillFeeRemainingList(
        OwnerPayableBillPayCommand command,
        List<OwnerPayableBillFee> feeList
    ) {
        List<Long> feeIds = feeList.stream().map(OwnerPayableBillFee::getId).toList();
        Map<Long, BigDecimal> settledAmountMap = financeFlowRepo
            .getListByBizIds(FinanceBizTypeEnum.OWNER_PAYABLE_BILL_FEE.getCode(), feeIds)
            .stream()
            .filter(item -> item.getBizId() != null)
            .filter(item -> FinanceFlowStatusEnum.SUCCESS.getCode().equals(item.getStatus()))
            .collect(Collectors.groupingBy(
                FinanceFlow::getBizId,
                Collectors.mapping(this::toOwnerPayableBillSignedAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));

        List<OwnerPayableBillFeeRemaining> remainingList = feeList.stream()
            .map(item -> new OwnerPayableBillFeeRemaining(
                item,
                toOwnerPayableBillFeeSignedAmount(item).subtract(defaultZero(settledAmountMap.get(item.getId())))
            ))
            .filter(item -> item.remainingSignedAmount().compareTo(BigDecimal.ZERO) != 0)
            .collect(Collectors.toCollection(ArrayList::new));

        BigDecimal distributedAmount = settledAmountMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal legacyPaidAmount = defaultZero(command.bill().getPaidAmount()).subtract(distributedAmount);
        if (legacyPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            applyLegacyPaidAmount(remainingList, legacyPaidAmount);
        }
        return remainingList;
    }

    private void applyLegacyPaidAmount(List<OwnerPayableBillFeeRemaining> remainingList, BigDecimal legacyPaidAmount) {
        List<OwnerPayableBillFeeAllocation> legacyAllocations = allocateOwnerPayableBillFeeAmount(remainingList, legacyPaidAmount);
        if (legacyAllocations.isEmpty()) {
            return;
        }
        Map<Long, BigDecimal> allocationMap = legacyAllocations.stream()
            .collect(Collectors.toMap(item -> item.fee().getId(), OwnerPayableBillFeeAllocation::signedAmount));
        for (int i = 0; i < remainingList.size(); i++) {
            OwnerPayableBillFeeRemaining item = remainingList.get(i);
            BigDecimal allocatedAmount = defaultZero(allocationMap.get(item.fee().getId()));
            remainingList.set(i, new OwnerPayableBillFeeRemaining(
                item.fee(),
                item.remainingSignedAmount().subtract(allocatedAmount)
            ));
        }
        remainingList.removeIf(item -> item.remainingSignedAmount().compareTo(BigDecimal.ZERO) == 0);
    }

    private List<OwnerPayableBillFeeAllocation> allocateOwnerPayableBillFeeAmount(
        List<OwnerPayableBillFeeRemaining> remainingList,
        BigDecimal paymentAmount
    ) {
        if (remainingList == null || remainingList.isEmpty() || paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        BigDecimal netRemainingAmount = remainingList.stream()
            .map(OwnerPayableBillFeeRemaining::remainingSignedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (netRemainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        if (paymentAmount.compareTo(netRemainingAmount) > 0) {
            BigDecimal diff = paymentAmount.subtract(netRemainingAmount).abs();
            if (diff.compareTo(new BigDecimal("0.01")) > 0) {
                throw new IllegalArgumentException("付款金额超过费用项剩余可分摊金额");
            }
            paymentAmount = netRemainingAmount;
        }

        BigDecimal ratio = paymentAmount.divide(netRemainingAmount, 10, RoundingMode.HALF_UP);
        List<OwnerPayableBillFeeAllocation> allocations = new ArrayList<>();
        BigDecimal allocatedNetAmount = BigDecimal.ZERO;
        for (OwnerPayableBillFeeRemaining item : remainingList) {
            BigDecimal signedAmount = item.remainingSignedAmount().multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            if (signedAmount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            allocations.add(new OwnerPayableBillFeeAllocation(item.fee(), signedAmount));
            allocatedNetAmount = allocatedNetAmount.add(signedAmount);
        }

        BigDecimal diff = paymentAmount.subtract(allocatedNetAmount);
        if (diff.compareTo(BigDecimal.ZERO) != 0 && !allocations.isEmpty()) {
            int index = findRoundingAdjustmentIndex(allocations, remainingList, diff);
            OwnerPayableBillFeeAllocation allocation = allocations.get(index);
            allocations.set(index, new OwnerPayableBillFeeAllocation(allocation.fee(), allocation.signedAmount().add(diff)));
        }
        return allocations.stream()
            .filter(item -> item.signedAmount().compareTo(BigDecimal.ZERO) != 0)
            .toList();
    }

    private int findRoundingAdjustmentIndex(
        List<OwnerPayableBillFeeAllocation> allocations,
        List<OwnerPayableBillFeeRemaining> remainingList,
        BigDecimal diff
    ) {
        Map<Long, BigDecimal> remainingMap = remainingList.stream()
            .collect(Collectors.toMap(item -> item.fee().getId(), OwnerPayableBillFeeRemaining::remainingSignedAmount));
        for (int i = allocations.size() - 1; i >= 0; i--) {
            OwnerPayableBillFeeAllocation allocation = allocations.get(i);
            BigDecimal adjustedAmount = allocation.signedAmount().add(diff);
            BigDecimal remainingAmount = remainingMap.get(allocation.fee().getId());
            if (adjustedAmount.compareTo(BigDecimal.ZERO) == allocation.signedAmount().compareTo(BigDecimal.ZERO)
                && adjustedAmount.abs().compareTo(remainingAmount.abs()) <= 0) {
                return i;
            }
        }
        return allocations.size() - 1;
    }

    private BigDecimal toOwnerPayableBillFeeSignedAmount(OwnerPayableBillFee fee) {
        BigDecimal amount = defaultZero(fee.getAmount()).abs();
        return FinanceFlowDirectionEnum.OUT.getCode().equals(fee.getDirection()) ? amount.negate() : amount;
    }

    private BigDecimal toOwnerPayableBillSignedAmount(FinanceFlow financeFlow) {
        BigDecimal amount = defaultZero(financeFlow.getAmount());
        return FinanceFlowDirectionEnum.IN.getCode().equals(financeFlow.getFlowDirection()) ? amount.negate() : amount;
    }

    private FinanceFlow buildFinanceFlow(CreateCommand command, LeaseBillFee fee, LeaseBillCollectDTO.Item item) {
        FinanceFlow financeFlow = new FinanceFlow();
        financeFlow.setFlowNo(generateFinanceFlowNo());
        financeFlow.setCompanyId(command.paymentFlow().getCompanyId());
        financeFlow.setPaymentFlowId(command.paymentFlow().getId());
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
        PaymentFlow paymentFlow,
        List<OwnerPayableBillFee> feeList,
        String ownerName,
        Long operatorId,
        String operatorName,
        String remark,
        DateTime now
    ) {
    }

    private record OwnerPayableBillFeeRemaining(
        OwnerPayableBillFee fee,
        BigDecimal remainingSignedAmount
    ) {
    }

    private record OwnerPayableBillFeeAllocation(
        OwnerPayableBillFee fee,
        BigDecimal signedAmount
    ) {
    }
}
