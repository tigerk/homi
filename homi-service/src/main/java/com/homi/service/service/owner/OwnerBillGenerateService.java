package com.homi.service.service.owner;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.lease.LeaseRentDueTypeEnum;
import com.homi.common.lib.enums.owner.*;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业主账单服务
 * <p>
 * 负责轻托管模式下起租日账单的自动生成。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerBillGenerateService {
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final OwnerSettlementRuleRepo ownerSettlementRuleRepo;
    private final OwnerRentFreeRuleRepo ownerRentFreeRuleRepo;
    private final OwnerLeaseRuleRepo ownerLeaseRuleRepo;
    private final OwnerLeaseFeeRepo ownerLeaseFeeRepo;
    private final OwnerLeaseFreeRuleRepo ownerLeaseFreeRuleRepo;
    private final OwnerBillRepo ownerBillRepo;
    private final OwnerBillLineRepo ownerBillLineRepo;
    private final OwnerBillPaymentRepo ownerBillPaymentRepo;
    private final OwnerBillReductionRepo ownerBillReductionRepo;
    private final OwnerAccountRepo ownerAccountRepo;
    private final OwnerAccountFlowRepo ownerAccountFlowRepo;

    /**
     * 自动生成起租日业主账单
     *
     * @return 成功生成的账单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer generateLeaseStartOwnerBills() {
        Date todayStart = DateUtil.beginOfDay(new Date());
        Date todayEnd = DateUtil.endOfDay(new Date());

        List<OwnerContract> contractList = ownerContractRepo.list(new LambdaQueryWrapper<OwnerContract>()
            .eq(OwnerContract::getCooperationMode, OwnerCooperationModeEnum.LIGHT_MANAGED.name())
            .eq(OwnerContract::getStatus, StatusEnum.ACTIVE.getValue())
            .eq(OwnerContract::getApprovalStatus, BizApprovalStatusEnum.APPROVED.getCode())
            .eq(OwnerContract::getSignStatus, OwnerSignStatusEnum.SIGNED.getCode())
            .le(OwnerContract::getContractStart, todayEnd));

        int successCount = 0;
        for (OwnerContract contract : contractList) {
            try {
                if (generateLeaseStartOwnerBillByContract(contract.getId(), todayStart, todayEnd)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("生成起租日业主账单失败, contractId={}", contract.getId(), e);
            }
        }
        return successCount;
    }

    /**
     * 重建包租合同的全部业主应付账单计划。
     * <p>
     * 在新增或编辑包租合同后立即调用，直接按合同周期生成完整账单。
     *
     * @param contractId 合同ID
     * @return 成功生成的账单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer rebuildMasterLeaseOwnerBillsByContract(Long contractId) {
        OwnerContract contract = ownerContractRepo.getById(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        if (!OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            return 0;
        }
        clearMasterLeaseBillsByContract(contractId);
        Date planEnd = resolveMasterLeasePlanEnd(contractId);
        if (planEnd == null) {
            return 0;
        }
        return generateMasterLeaseOwnerBillsByContract(contractId, planEnd);
    }

    /**
     * 清空包租合同下尚未发生结算的账单计划。
     *
     * @param contractId 合同ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearMasterLeaseOwnerBillsByContract(Long contractId) {
        clearMasterLeaseBillsByContract(contractId);
    }

    /**
     * 判断包租合同账单条款是否已锁定。
     * <p>
     * 一旦该合同下已有付款记录，或账单已发生已结/已提现/冻结金额变化，则不允许再重建账单计划。
     *
     * @param contractId 合同ID
     * @return 是否锁定
     */
    public boolean isMasterLeaseBillLocked(Long contractId) {
        List<OwnerBill> billList = ownerBillRepo.lambdaQuery()
            .eq(OwnerBill::getContractId, contractId)
            .list();
        if (billList.isEmpty()) {
            return false;
        }
        boolean hasSettledBill = billList.stream().anyMatch(item ->
            ObjectUtil.defaultIfNull(item.getSettledAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
                || ObjectUtil.defaultIfNull(item.getWithdrawnAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
                || ObjectUtil.defaultIfNull(item.getFreezeAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
        );
        if (hasSettledBill) {
            return true;
        }
        List<Long> billIds = billList.stream().map(OwnerBill::getId).toList();
        return ownerBillPaymentRepo.lambdaQuery()
            .in(OwnerBillPayment::getBillId, billIds)
            .count() > 0;
    }

    /**
     * 按合同生成起租日业主账单
     *
     * @param contractId 合同ID
     * @param todayStart 当天开始时间
     * @param todayEnd   当天结束时间
     * @return 是否成功生成账单
     */
    public boolean generateLeaseStartOwnerBillByContract(Long contractId, Date todayStart, Date todayEnd) {
        OwnerContract contract = ownerContractRepo.getById(contractId);
        if (contract == null || !isLeaseStartBillContract(contract)) {
            return false;
        }
        if (contract.getContractStart() == null || contract.getContractStart().after(todayEnd)) {
            return false;
        }

        Date billDate = DateUtil.beginOfDay(contract.getContractStart());

        List<OwnerContractSubject> contractSubjectList = ownerContractSubjectRepo.listByContractId(contractId);
        if (contractSubjectList.isEmpty()) {
            return false;
        }

        Map<Long, OwnerSettlementRule> settlementRuleMap = ownerSettlementRuleRepo.list(new LambdaQueryWrapper<OwnerSettlementRule>()
                .eq(OwnerSettlementRule::getContractId, contractId)
                .eq(OwnerSettlementRule::getStatus, StatusEnum.ACTIVE.getValue()))
            .stream()
            .filter(item -> OwnerSettlementTimingEnum.LEASE_START_GENERATE_BILL.name().equals(item.getSettlementTiming()))
            .collect(Collectors.toMap(OwnerSettlementRule::getContractSubjectId, Function.identity(), (left, right) -> left));

        if (settlementRuleMap.isEmpty()) {
            return false;
        }

        Map<Long, OwnerRentFreeRule> rentFreeRuleMap = ownerRentFreeRuleRepo.list(new LambdaQueryWrapper<OwnerRentFreeRule>()
                .eq(OwnerRentFreeRule::getContractId, contractId)
                .eq(OwnerRentFreeRule::getStatus, StatusEnum.ACTIVE.getValue()))
            .stream()
            .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
            .collect(Collectors.toMap(OwnerRentFreeRule::getContractSubjectId, Function.identity(), (left, right) -> left));

        boolean created = false;
        for (OwnerContractSubject contractSubject : contractSubjectList) {
            OwnerSettlementRule settlementRule = settlementRuleMap.get(contractSubject.getId());
            if (settlementRule == null) {
                continue;
            }
            if (existsLeaseStartBill(contractId, contractSubject.getSubjectId(), billDate)) {
                continue;
            }

            BigDecimal rentAmount = resolveLeaseStartRentAmount(settlementRule);
            if (rentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("起租日账单未生成标的明细，结算方式暂不支持自动计算, contractId={}, contractSubjectId={}, settlementMode={}",
                    contractId, contractSubject.getId(), settlementRule.getSettlementMode());
                continue;
            }

            BigDecimal incomeAmount = rentAmount;
            BigDecimal expenseAmount = BigDecimal.ZERO;
            List<OwnerBillLine> lineList = new ArrayList<>();
            List<String> remarkList = new ArrayList<>();

            OwnerRentFreeRule rentFreeRule = rentFreeRuleMap.get(contractSubject.getId());
            if (isMatchedRentFreeRule(billDate, rentFreeRule)) {
                remarkList.add("合同房源【" + CharSequenceUtil.nullToDefault(contractSubject.getSubjectNameSnapshot(), "") + "】命中免租规则，当前未自动冲减，请人工复核。");
            }

            lineList.add(buildRentLine(contractSubject, settlementRule, billDate, rentAmount));

            BigDecimal managementFeeAmount = calcManagementFee(settlementRule, rentAmount);
            if (managementFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
                expenseAmount = expenseAmount.add(managementFeeAmount);
                lineList.add(buildManagementFeeLine(contractSubject, billDate, managementFeeAmount));
            }

            BigDecimal payableAmount = incomeAmount.subtract(expenseAmount);
            BigDecimal withdrawableAmount = payableAmount.compareTo(BigDecimal.ZERO) > 0 ? payableAmount : BigDecimal.ZERO;
            Date now = DateUtil.date();

            OwnerBill ownerBill = new OwnerBill();
            ownerBill.setCompanyId(contract.getCompanyId());
            ownerBill.setOwnerId(contract.getOwnerId());
            ownerBill.setContractId(contract.getId());
            ownerBill.setSubjectType(contractSubject.getSubjectType());
            ownerBill.setSubjectId(contractSubject.getSubjectId());
            ownerBill.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
            ownerBill.setBillNo(generateOwnerBillNo());
            ownerBill.setBillBizType(OwnerBillBizTypeEnum.LIGHT_MANAGED_SETTLEMENT.getCode());
            ownerBill.setBillStart(billDate);
            ownerBill.setBillEnd(billDate);
            ownerBill.setIncomeAmount(incomeAmount);
            ownerBill.setReductionAmount(BigDecimal.ZERO);
            ownerBill.setExpenseAmount(expenseAmount);
            ownerBill.setAdjustAmount(BigDecimal.ZERO);
            ownerBill.setPayableAmount(payableAmount);
            ownerBill.setSettledAmount(BigDecimal.ZERO);
            ownerBill.setWithdrawnAmount(BigDecimal.ZERO);
            ownerBill.setFreezeAmount(BigDecimal.ZERO);
            ownerBill.setWithdrawableAmount(withdrawableAmount);
            ownerBill.setBillStatus(OwnerBillStatusEnum.NORMAL.getCode());
            ownerBill.setApprovalStatus(BizApprovalStatusEnum.APPROVED.getCode());
            ownerBill.setSettlementStatus(OwnerBillSettlementStatusEnum.UNSETTLED.getCode());
            ownerBill.setGeneratedAt(now);
            ownerBill.setApprovedAt(now);
            ownerBill.setRemark(remarkList.isEmpty() ? "起租日自动生成账单" : String.join("；", remarkList));
            ownerBill.setCreateTime(now);
            ownerBill.setUpdateTime(now);
            ownerBillRepo.save(ownerBill);

            lineList.forEach(item -> {
                item.setBillId(ownerBill.getId());
                item.setCreateTime(now);
            });
            ownerBillLineRepo.saveBatch(lineList);

            if (withdrawableAmount.compareTo(BigDecimal.ZERO) > 0) {
                increaseOwnerAccountAmount(contract, ownerBill, withdrawableAmount, now);
            }
            created = true;
        }
        return created;
    }

    private boolean isLeaseStartBillContract(OwnerContract contract) {
        return Objects.equals(contract.getStatus(), StatusEnum.ACTIVE.getValue())
            && Objects.equals(contract.getApprovalStatus(), BizApprovalStatusEnum.APPROVED.getCode())
            && Objects.equals(contract.getSignStatus(), OwnerSignStatusEnum.SIGNED.getCode())
            && OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode());
    }

    private int generateMasterLeaseOwnerBillsByContract(Long contractId, Date todayEnd) {
        OwnerContract contract = ownerContractRepo.getById(contractId);
        if (contract == null || !OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            return 0;
        }

        OwnerLeaseRule leaseRule = ownerLeaseRuleRepo.lambdaQuery()
            .eq(OwnerLeaseRule::getContractId, contractId)
            .eq(OwnerLeaseRule::getStatus, StatusEnum.ACTIVE.getValue())
            .orderByDesc(OwnerLeaseRule::getId)
            .last("limit 1")
            .one();
        if (leaseRule == null) {
            return 0;
        }

        Date billingStart = resolveMasterLeaseBillingStart(contract, leaseRule);
        Date billingEnd = resolveMasterLeaseBillingEnd(contract, leaseRule);
        if (billingStart == null || billingEnd == null || billingStart.after(todayEnd) || billingStart.after(billingEnd)) {
            return 0;
        }

        int paymentMonths = Math.max(1, ObjectUtil.defaultIfNull(leaseRule.getPaymentMonths(), 1));
        List<OwnerLeaseFee> leaseFeeList = ownerLeaseFeeRepo.lambdaQuery()
            .eq(OwnerLeaseFee::getContractId, contractId)
            .eq(OwnerLeaseFee::getStatus, StatusEnum.ACTIVE.getValue())
            .orderByAsc(OwnerLeaseFee::getSortOrder)
            .orderByAsc(OwnerLeaseFee::getId)
            .list();
        List<OwnerLeaseFreeRule> leaseFreeRuleList = ownerLeaseFreeRuleRepo.lambdaQuery()
            .eq(OwnerLeaseFreeRule::getContractId, contractId)
            .eq(OwnerLeaseFreeRule::getStatus, StatusEnum.ACTIVE.getValue())
            .orderByAsc(OwnerLeaseFreeRule::getId)
            .list();

        int createdCount = 0;
        Date periodStart = DateUtil.beginOfDay(billingStart);
        Date firstPeriodStart = periodStart;
        while (!periodStart.after(todayEnd) && !periodStart.after(billingEnd)) {
            Date periodEnd = resolveMasterLeasePeriodEnd(periodStart, paymentMonths, billingEnd);
            if (!existsMasterLeaseBill(contractId, periodStart, periodEnd)) {
                createMasterLeaseBill(contract, leaseRule, leaseFeeList, leaseFreeRuleList, periodStart, periodEnd, firstPeriodStart.equals(periodStart));
                createdCount++;
            }
            periodStart = DateUtil.beginOfDay(DateUtil.offsetMonth(periodStart, paymentMonths));
        }
        return createdCount;
    }

    private Date resolveMasterLeasePlanEnd(Long contractId) {
        OwnerContract contract = ownerContractRepo.getById(contractId);
        if (contract == null) {
            return null;
        }
        OwnerLeaseRule leaseRule = ownerLeaseRuleRepo.lambdaQuery()
            .eq(OwnerLeaseRule::getContractId, contractId)
            .eq(OwnerLeaseRule::getStatus, StatusEnum.ACTIVE.getValue())
            .orderByDesc(OwnerLeaseRule::getId)
            .last("limit 1")
            .one();
        if (leaseRule == null) {
            return null;
        }
        return resolveMasterLeaseBillingEnd(contract, leaseRule);
    }

    private void clearMasterLeaseBillsByContract(Long contractId) {
        List<OwnerBill> billList = ownerBillRepo.lambdaQuery()
            .eq(OwnerBill::getContractId, contractId)
            .orderByAsc(OwnerBill::getId)
            .list();
        if (billList.isEmpty()) {
            return;
        }
        if (isMasterLeaseBillLocked(contractId)) {
            throw new IllegalArgumentException("包租账单已发生付款或结算，账单条款已锁定");
        }

        List<Long> billIds = billList.stream().map(OwnerBill::getId).toList();
        ownerBillReductionRepo.remove(new LambdaQueryWrapper<OwnerBillReduction>().in(OwnerBillReduction::getBillId, billIds));
        ownerBillLineRepo.remove(new LambdaQueryWrapper<OwnerBillLine>().in(OwnerBillLine::getBillId, billIds));
        ownerBillRepo.removeByIds(billIds);
    }

    private boolean isMasterLeaseBillContract(OwnerContract contract) {
        return Objects.equals(contract.getStatus(), StatusEnum.ACTIVE.getValue())
            && Objects.equals(contract.getApprovalStatus(), BizApprovalStatusEnum.APPROVED.getCode())
            && Objects.equals(contract.getSignStatus(), OwnerSignStatusEnum.SIGNED.getCode())
            && OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode());
    }

    private Date resolveMasterLeaseBillingStart(OwnerContract contract, OwnerLeaseRule leaseRule) {
        if (leaseRule.getBillingStart() != null) {
            return DateUtil.beginOfDay(leaseRule.getBillingStart());
        }
        if (leaseRule.getFirstPayDate() != null) {
            return DateUtil.beginOfDay(leaseRule.getFirstPayDate());
        }
        if (contract.getContractStart() != null) {
            return DateUtil.beginOfDay(contract.getContractStart());
        }
        return null;
    }

    private Date resolveMasterLeaseBillingEnd(OwnerContract contract, OwnerLeaseRule leaseRule) {
        if (leaseRule.getBillingEnd() != null) {
            return DateUtil.endOfDay(leaseRule.getBillingEnd());
        }
        if (contract.getContractEnd() != null) {
            return DateUtil.endOfDay(contract.getContractEnd());
        }
        return null;
    }

    private Date resolveMasterLeasePeriodEnd(Date periodStart, int paymentMonths, Date billingEnd) {
        Date currentPeriodEnd = DateUtil.endOfDay(DateUtil.offsetDay(DateUtil.offsetMonth(periodStart, paymentMonths), -1));
        if (billingEnd != null && currentPeriodEnd.after(billingEnd)) {
            return DateUtil.endOfDay(billingEnd);
        }
        return currentPeriodEnd;
    }

    private boolean existsMasterLeaseBill(Long contractId, Date billStart, Date billEnd) {
        return ownerBillRepo.count(new LambdaQueryWrapper<OwnerBill>()
            .eq(OwnerBill::getContractId, contractId)
            .eq(OwnerBill::getBillStart, billStart)
            .eq(OwnerBill::getBillEnd, billEnd)) > 0;
    }

    private void createMasterLeaseBill(
        OwnerContract contract,
        OwnerLeaseRule leaseRule,
        List<OwnerLeaseFee> leaseFeeList,
        List<OwnerLeaseFreeRule> leaseFreeRuleList,
        Date periodStart,
        Date periodEnd,
        boolean firstPeriod
    ) {
        Date now = new Date();
        List<OwnerContractSubject> subjectList = ownerContractSubjectRepo.listByContractId(contract.getId());
        String subjectSummary = buildMasterLeaseSubjectSummary(subjectList);

        BigDecimal incomeAmount = BigDecimal.ZERO;
        BigDecimal expenseAmount = BigDecimal.ZERO;
        BigDecimal reductionAmount = BigDecimal.ZERO;
        List<OwnerBillLine> lineList = new ArrayList<>();
        List<OwnerBillReduction> reductionList = new ArrayList<>();

        BigDecimal rentAmount = calcMasterLeaseRentAmount(leaseRule, periodStart, periodEnd);
        if (rentAmount.compareTo(BigDecimal.ZERO) > 0) {
            incomeAmount = incomeAmount.add(rentAmount);
            lineList.add(buildMasterLeaseLine(contract, periodStart, OwnerBillSourceTypeEnum.OWNER_CONTRACT.getCode(), contract.getId(),
                OwnerBillItemTypeEnum.RENT.getCode(), OwnerBillItemTypeEnum.RENT.getName(), FinanceFlowDirectionEnum.IN.getCode(), rentAmount,
                "包租周期租金", "月租金 " + ObjectUtil.defaultIfNull(leaseRule.getRentAmount(), BigDecimal.ZERO) + "，按账期自动生成"));
        }

        if (firstPeriod && ObjectUtil.defaultIfNull(leaseRule.getDepositAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal depositAmount = ObjectUtil.defaultIfNull(leaseRule.getDepositAmount(), BigDecimal.ZERO);
            incomeAmount = incomeAmount.add(depositAmount);
            lineList.add(buildMasterLeaseLine(contract, periodStart, OwnerBillSourceTypeEnum.OWNER_CONTRACT.getCode(), contract.getId(),
                OwnerBillItemTypeEnum.DEPOSIT.getCode(), OwnerBillItemTypeEnum.DEPOSIT.getName(), FinanceFlowDirectionEnum.IN.getCode(), depositAmount,
                "首期押金", "包租首期押金"));
        }

        for (OwnerLeaseFee leaseFee : leaseFeeList) {
            BigDecimal feeAmount = calcMasterLeaseFeeAmount(leaseRule, leaseFee, periodStart, periodEnd);
            if (feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            String direction = ObjectUtil.defaultIfNull(leaseFee.getFeeDirection(), FinanceFlowDirectionEnum.IN.getCode());
            if (FinanceFlowDirectionEnum.OUT.getCode().equals(direction)) {
                expenseAmount = expenseAmount.add(feeAmount);
            } else {
                incomeAmount = incomeAmount.add(feeAmount);
            }
            lineList.add(buildMasterLeaseLine(contract, periodStart, OwnerBillSourceTypeEnum.OWNER_LEASE_FEE.getCode(), leaseFee.getId(),
                OwnerBillItemTypeEnum.OTHER_FEE.getCode(), ObjectUtil.defaultIfNull(leaseFee.getFeeName(), "其他费用"), direction, feeAmount,
                leaseFee.getRemark(), buildMasterLeaseFeeFormula(leaseRule, leaseFee, periodStart, periodEnd)));
        }

        for (OwnerLeaseFreeRule freeRule : leaseFreeRuleList) {
            BigDecimal currentReductionAmount = calcMasterLeaseReductionAmount(rentAmount, freeRule, periodStart, periodEnd);
            if (currentReductionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            reductionAmount = reductionAmount.add(currentReductionAmount);
            reductionList.add(buildMasterLeaseReduction(contract, freeRule, periodStart, currentReductionAmount, now));
        }

        BigDecimal payableAmount = incomeAmount.subtract(expenseAmount).subtract(reductionAmount);

        OwnerBill ownerBill = new OwnerBill();
        ownerBill.setCompanyId(contract.getCompanyId());
        ownerBill.setOwnerId(contract.getOwnerId());
        ownerBill.setContractId(contract.getId());
        ownerBill.setSubjectType(null);
        ownerBill.setSubjectId(null);
        ownerBill.setSubjectNameSnapshot(subjectSummary);
        ownerBill.setBillNo(generateOwnerBillNo());
        ownerBill.setBillBizType(OwnerBillBizTypeEnum.MASTER_LEASE_PAYABLE.getCode());
        ownerBill.setBillStart(periodStart);
        ownerBill.setBillEnd(periodEnd);
        ownerBill.setDueDate(resolveMasterLeaseDueDate(leaseRule, periodStart));
        ownerBill.setIncomeAmount(incomeAmount);
        ownerBill.setReductionAmount(reductionAmount);
        ownerBill.setExpenseAmount(expenseAmount);
        ownerBill.setAdjustAmount(BigDecimal.ZERO);
        ownerBill.setPayableAmount(payableAmount);
        ownerBill.setSettledAmount(BigDecimal.ZERO);
        ownerBill.setWithdrawnAmount(BigDecimal.ZERO);
        ownerBill.setFreezeAmount(BigDecimal.ZERO);
        ownerBill.setWithdrawableAmount(BigDecimal.ZERO);
        ownerBill.setBillStatus(OwnerBillStatusEnum.NORMAL.getCode());
        ownerBill.setApprovalStatus(BizApprovalStatusEnum.APPROVED.getCode());
        ownerBill.setSettlementStatus(OwnerBillSettlementStatusEnum.UNSETTLED.getCode());
        ownerBill.setGeneratedAt(now);
        ownerBill.setApprovedAt(now);
        ownerBill.setRemark("包租应付账单自动生成");
        ownerBill.setCreateTime(now);
        ownerBill.setUpdateTime(now);
        ownerBillRepo.save(ownerBill);

        lineList.forEach(item -> {
            item.setBillId(ownerBill.getId());
            item.setSubjectNameSnapshot(subjectSummary);
            item.setCreateTime(now);
        });
        if (!lineList.isEmpty()) {
            ownerBillLineRepo.saveBatch(lineList);
        }

        reductionList.forEach(item -> {
            item.setBillId(ownerBill.getId());
            item.setCreateTime(now);
            item.setUpdateTime(now);
        });
        if (!reductionList.isEmpty()) {
            ownerBillReductionRepo.saveBatch(reductionList);
        }
    }

    private String buildMasterLeaseSubjectSummary(List<OwnerContractSubject> subjectList) {
        if (subjectList == null || subjectList.isEmpty()) {
            return "包租合同房源";
        }
        if (subjectList.size() == 1) {
            return CharSequenceUtil.nullToDefault(subjectList.get(0).getSubjectNameSnapshot(), "包租合同房源");
        }
        return subjectList.stream()
            .map(OwnerContractSubject::getSubjectNameSnapshot)
            .filter(StrUtil::isNotBlank)
            .limit(2)
            .collect(Collectors.joining("、")) + " 等" + subjectList.size() + "项";
    }

    private BigDecimal calcMasterLeaseRentAmount(OwnerLeaseRule leaseRule, Date periodStart, Date periodEnd) {
        BigDecimal monthlyRent = ObjectUtil.defaultIfNull(leaseRule.getRentAmount(), BigDecimal.ZERO);
        if (monthlyRent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        int paymentMonths = Math.max(1, ObjectUtil.defaultIfNull(leaseRule.getPaymentMonths(), 1));
        Date fullPeriodEnd = DateUtil.endOfDay(DateUtil.offsetDay(DateUtil.offsetMonth(periodStart, paymentMonths), -1));
        BigDecimal fullAmount = monthlyRent.multiply(BigDecimal.valueOf(paymentMonths));
        if (!periodEnd.before(fullPeriodEnd)) {
            return fullAmount.setScale(2, RoundingMode.HALF_UP);
        }
        if (OwnerProrateTypeEnum.FULL_PERIOD.getCode().equals(leaseRule.getProrateType())) {
            return fullAmount.setScale(2, RoundingMode.HALF_UP);
        }
        long fullDays = DateUtil.betweenDay(periodStart, fullPeriodEnd, true) + 1;
        long actualDays = DateUtil.betweenDay(periodStart, periodEnd, true) + 1;
        if (fullDays <= 0 || actualDays <= 0) {
            return BigDecimal.ZERO;
        }
        return fullAmount.multiply(BigDecimal.valueOf(actualDays))
            .divide(BigDecimal.valueOf(fullDays), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcMasterLeaseFeeAmount(OwnerLeaseRule leaseRule, OwnerLeaseFee leaseFee, Date periodStart, Date periodEnd) {
        int occurrenceCount = countMasterLeaseFeeOccurrences(resolveMasterLeaseBillingAnchor(leaseRule), periodStart, periodEnd, leaseFee.getPaymentMethod());
        if (occurrenceCount <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal unitAmount;
        if (Integer.valueOf(2).equals(leaseFee.getPriceMethod())) {
            unitAmount = ObjectUtil.defaultIfNull(leaseRule.getRentAmount(), BigDecimal.ZERO)
                .multiply(ObjectUtil.defaultIfNull(leaseFee.getPriceInput(), BigDecimal.ZERO))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            unitAmount = ObjectUtil.defaultIfNull(leaseFee.getPriceInput(), BigDecimal.ZERO);
        }
        return unitAmount.multiply(BigDecimal.valueOf(occurrenceCount)).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildMasterLeaseFeeFormula(OwnerLeaseRule leaseRule, OwnerLeaseFee leaseFee, Date periodStart, Date periodEnd) {
        int occurrenceCount = countMasterLeaseFeeOccurrences(resolveMasterLeaseBillingAnchor(leaseRule), periodStart, periodEnd, leaseFee.getPaymentMethod());
        String formula = Integer.valueOf(2).equals(leaseFee.getPriceMethod())
            ? "月租金 × " + ObjectUtil.defaultIfNull(leaseFee.getPriceInput(), BigDecimal.ZERO) + "%"
            : "固定金额 " + ObjectUtil.defaultIfNull(leaseFee.getPriceInput(), BigDecimal.ZERO);
        return formula + " × " + occurrenceCount;
    }

    private int countMasterLeaseFeeOccurrences(Date anchorDate, Date periodStart, Date periodEnd, Integer paymentMethod) {
        if (anchorDate == null) {
            return 0;
        }
        if (paymentMethod == null || Integer.valueOf(0).equals(paymentMethod)) {
            return 1;
        }
        if (Integer.valueOf(1).equals(paymentMethod)) {
            return DateUtil.beginOfDay(anchorDate).equals(DateUtil.beginOfDay(periodStart)) ? 1 : 0;
        }
        int intervalMonths = switch (paymentMethod) {
            case 2 -> 1;
            case 3 -> 2;
            case 4 -> 3;
            case 5 -> 6;
            case 6 -> 12;
            default -> 0;
        };
        if (intervalMonths <= 0) {
            return 0;
        }
        int count = 0;
        Date current = DateUtil.beginOfDay(anchorDate);
        while (!current.after(periodEnd)) {
            if (!current.before(periodStart)) {
                count++;
            }
            current = DateUtil.beginOfDay(DateUtil.offsetMonth(current, intervalMonths));
        }
        return count;
    }

    private Date resolveMasterLeaseBillingAnchor(OwnerLeaseRule leaseRule) {
        if (leaseRule.getBillingStart() != null) {
            return DateUtil.beginOfDay(leaseRule.getBillingStart());
        }
        if (leaseRule.getFirstPayDate() != null) {
            return DateUtil.beginOfDay(leaseRule.getFirstPayDate());
        }
        return null;
    }

    /**
     * 按付款设置计算包租账单的应付日期。
     * <p>
     * 规则与租客账单一致：
     * - 提前付款：账期开始日 - 偏移天数
     * - 固定付款：账期开始所在月份的固定日期
     * - 延后付款：账期开始日 + 偏移天数
     */
    private Date resolveMasterLeaseDueDate(OwnerLeaseRule leaseRule, Date periodStart) {
        if (leaseRule == null || periodStart == null) {
            return null;
        }
        LocalDate periodStartDate = DateUtil.toLocalDateTime(periodStart).toLocalDate();
        Integer rentDueType = ObjectUtil.defaultIfNull(leaseRule.getRentDueType(), LeaseRentDueTypeEnum.FIXED.getCode());
        if (Objects.equals(rentDueType, LeaseRentDueTypeEnum.EARLY.getCode())) {
            int offsetDays = ObjectUtil.defaultIfNull(leaseRule.getRentDueOffsetDays(), 0);
            return DateUtil.date(periodStartDate.minusDays(offsetDays));
        }
        if (Objects.equals(rentDueType, LeaseRentDueTypeEnum.LATE.getCode())) {
            int offsetDays = ObjectUtil.defaultIfNull(leaseRule.getRentDueOffsetDays(), 0);
            return DateUtil.date(periodStartDate.plusDays(offsetDays));
        }

        Integer rentDueDay = ObjectUtil.defaultIfNull(leaseRule.getRentDueDay(), 0);
        int actualDay = rentDueDay == null || rentDueDay <= 0
            ? periodStartDate.lengthOfMonth()
            : Math.min(rentDueDay, periodStartDate.lengthOfMonth());
        return DateUtil.date(periodStartDate.withDayOfMonth(actualDay));
    }

    private BigDecimal calcMasterLeaseReductionAmount(BigDecimal rentAmount, OwnerLeaseFreeRule freeRule, Date periodStart, Date periodEnd) {
        if (freeRule == null || freeRule.getStartDate() == null || freeRule.getEndDate() == null) {
            return BigDecimal.ZERO;
        }
        Date overlapStart = DateUtil.beginOfDay(freeRule.getStartDate());
        Date overlapEnd = DateUtil.endOfDay(freeRule.getEndDate());
        if (periodEnd.before(overlapStart) || periodStart.after(overlapEnd)) {
            return BigDecimal.ZERO;
        }
        if (OwnerFreeCalcModeEnum.BY_DAYS.getCode().equals(freeRule.getCalcMode())) {
            Date actualStart = periodStart.after(overlapStart) ? periodStart : overlapStart;
            Date actualEnd = periodEnd.before(overlapEnd) ? periodEnd : overlapEnd;
            long fullDays = DateUtil.betweenDay(periodStart, periodEnd, true) + 1;
            long freeDays = DateUtil.betweenDay(actualStart, actualEnd, true) + 1;
            if (fullDays <= 0 || freeDays <= 0) {
                return BigDecimal.ZERO;
            }
            return ObjectUtil.defaultIfNull(rentAmount, BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(freeDays))
                .divide(BigDecimal.valueOf(fullDays), 2, RoundingMode.HALF_UP);
        }
        if (OwnerFreeCalcModeEnum.RATIO.getCode().equals(freeRule.getCalcMode())) {
            return ObjectUtil.defaultIfNull(rentAmount, BigDecimal.ZERO)
                .multiply(ObjectUtil.defaultIfNull(freeRule.getFreeRatio(), BigDecimal.ZERO))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return ObjectUtil.defaultIfNull(freeRule.getFreeAmount(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private OwnerBillLine buildMasterLeaseLine(
        OwnerContract contract,
        Date bizDate,
        String sourceType,
        Long sourceId,
        String itemType,
        String itemName,
        String direction,
        BigDecimal amount,
        String remark,
        String formulaSnapshot
    ) {
        OwnerBillLine line = new OwnerBillLine();
        line.setSourceType(sourceType);
        line.setSourceId(sourceId);
        line.setSubjectType(null);
        line.setSubjectId(null);
        line.setItemType(itemType);
        line.setItemName(itemName);
        line.setDirection(direction);
        line.setAmount(amount);
        line.setBizDate(bizDate);
        line.setRemark(remark);
        line.setFormulaSnapshot(formulaSnapshot);
        return line;
    }

    private OwnerBillReduction buildMasterLeaseReduction(OwnerContract contract, OwnerLeaseFreeRule freeRule, Date bizDate, BigDecimal amount, Date now) {
        OwnerBillReduction reduction = new OwnerBillReduction();
        reduction.setCompanyId(contract.getCompanyId());
        reduction.setOwnerId(contract.getOwnerId());
        reduction.setSourceType(OwnerBillSourceTypeEnum.OWNER_LEASE_FREE_RULE.getCode());
        reduction.setSourceId(freeRule.getId());
        reduction.setReductionType("LEASE_FREE");
        reduction.setReductionName("包租免租");
        reduction.setAmount(amount);
        reduction.setBizDate(bizDate);
        reduction.setRemark(freeRule.getRemark());
        reduction.setRuleSnapshot("calcMode=" + freeRule.getCalcMode());
        reduction.setStatus(StatusEnum.ACTIVE.getValue());
        reduction.setCreateTime(now);
        reduction.setUpdateTime(now);
        return reduction;
    }

    private boolean existsLeaseStartBill(Long contractId, Long subjectId, Date billDate) {
        return ownerBillRepo.count(new LambdaQueryWrapper<OwnerBill>()
            .eq(OwnerBill::getContractId, contractId)
            .eq(OwnerBill::getSubjectId, subjectId)
            .eq(OwnerBill::getBillStart, billDate)
            .eq(OwnerBill::getBillEnd, billDate)) > 0;
    }

    /**
     * 起租日账单仅处理可以直接确定金额的规则。
     * <p>
     * 目前支持：
     * - 固定结算：按保底金额/固定金额生成
     * - 保底 + 分成：先生成保底部分
     */
    private BigDecimal resolveLeaseStartRentAmount(OwnerSettlementRule settlementRule) {
        OwnerSettlementModeEnum settlementMode = resolveSettlementMode(settlementRule);
        if (settlementMode == null) {
            return BigDecimal.ZERO;
        }
        return switch (settlementMode) {
            case FIXED, GUARANTEE_PLUS_SHARE -> ObjectUtil.defaultIfNull(settlementRule.getGuaranteedRentAmount(), BigDecimal.ZERO);
            default -> BigDecimal.ZERO;
        };
    }

    private boolean isMatchedRentFreeRule(Date billDate, OwnerRentFreeRule rentFreeRule) {
        if (rentFreeRule == null || billDate == null || rentFreeRule.getStartDate() == null || rentFreeRule.getEndDate() == null) {
            return false;
        }
        Date begin = DateUtil.beginOfDay(rentFreeRule.getStartDate());
        Date end = DateUtil.endOfDay(rentFreeRule.getEndDate());
        return !billDate.before(begin) && !billDate.after(end);
    }

    private BigDecimal calcManagementFee(OwnerSettlementRule settlementRule, BigDecimal baseAmount) {
        if (settlementRule == null || !Boolean.TRUE.equals(settlementRule.getManagementFeeEnabled())) {
            return BigDecimal.ZERO;
        }
        BigDecimal managementFeeValue = ObjectUtil.defaultIfNull(settlementRule.getManagementFeeValue(), BigDecimal.ZERO);
        if (managementFeeValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (OwnerSettlementModeEnum.FIXED.getCode().equals(settlementRule.getManagementFeeMode())) {
            return managementFeeValue;
        }
        return baseAmount.multiply(managementFeeValue)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private OwnerBillLine buildRentLine(OwnerContractSubject contractSubject, OwnerSettlementRule settlementRule, Date billDate, BigDecimal amount) {
        OwnerBillLine line = new OwnerBillLine();
        line.setSourceType(OwnerBillSourceTypeEnum.OWNER_CONTRACT_SUBJECT.getCode());
        line.setSourceId(contractSubject.getId());
        line.setSubjectType(contractSubject.getSubjectType());
        line.setSubjectId(contractSubject.getSubjectId());
        line.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
        line.setItemType(OwnerBillItemTypeEnum.RENT.getCode());
        line.setItemName(buildRentLineName(settlementRule));
        line.setDirection(FinanceFlowDirectionEnum.IN.getCode());
        line.setAmount(amount);
        line.setBizDate(billDate);
        line.setRemark(contractSubject.getSubjectNameSnapshot());
        line.setFormulaSnapshot("leaseStartBill");
        return line;
    }

    private OwnerBillLine buildManagementFeeLine(OwnerContractSubject contractSubject, Date billDate, BigDecimal amount) {
        OwnerBillLine line = new OwnerBillLine();
        line.setSourceType(OwnerBillSourceTypeEnum.OWNER_CONTRACT_SUBJECT.getCode());
        line.setSourceId(contractSubject.getId());
        line.setSubjectType(contractSubject.getSubjectType());
        line.setSubjectId(contractSubject.getSubjectId());
        line.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
        line.setItemType(OwnerBillItemTypeEnum.MANAGEMENT_FEE.getCode());
        line.setItemName("管理费");
        line.setDirection(FinanceFlowDirectionEnum.OUT.getCode());
        line.setAmount(amount);
        line.setBizDate(billDate);
        line.setRemark(contractSubject.getSubjectNameSnapshot());
        line.setFormulaSnapshot("managementFee");
        return line;
    }

    private String buildRentLineName(OwnerSettlementRule settlementRule) {
        OwnerSettlementModeEnum settlementMode = resolveSettlementMode(settlementRule);
        if (settlementMode == null) {
            return "起租日结算";
        }
        return switch (settlementMode) {
            case FIXED -> "起租日固定结算";
            case GUARANTEE_PLUS_SHARE -> "起租日保底结算";
            default -> "起租日结算";
        };
    }

    private OwnerSettlementModeEnum resolveSettlementMode(OwnerSettlementRule settlementRule) {
        if (settlementRule == null || StrUtil.isBlank(settlementRule.getSettlementMode())) {
            return null;
        }
        try {
            return OwnerSettlementModeEnum.valueOf(settlementRule.getSettlementMode());
        } catch (IllegalArgumentException e) {
            log.warn("未知的轻托管结算方式, settlementMode={}", settlementRule.getSettlementMode());
            return null;
        }
    }

    private void increaseOwnerAccountAmount(OwnerContract contract, OwnerBill ownerBill, BigDecimal amount, Date now) {
        OwnerAccount account = ownerAccountRepo.getByOwnerId(contract.getOwnerId());
        if (account == null) {
            log.warn("业主账户不存在，跳过账户入账, ownerId={}, contractId={}", contract.getOwnerId(), contract.getId());
            return;
        }

        BigDecimal availableBefore = ObjectUtil.defaultIfNull(account.getAvailableAmount(), BigDecimal.ZERO);
        BigDecimal frozenBefore = ObjectUtil.defaultIfNull(account.getFrozenAmount(), BigDecimal.ZERO);
        account.setAvailableAmount(availableBefore.add(amount));
        account.setTotalIncomeAmount(ObjectUtil.defaultIfNull(account.getTotalIncomeAmount(), BigDecimal.ZERO).add(amount));
        account.setUpdateTime(now);
        ownerAccountRepo.updateById(account);

        OwnerAccountFlow flow = new OwnerAccountFlow();
        flow.setCompanyId(contract.getCompanyId());
        flow.setOwnerId(contract.getOwnerId());
        flow.setBizType(OwnerAccountFlowBizTypeEnum.OWNER_BILL.getCode());
        flow.setBizId(ownerBill.getId());
        flow.setFlowDirection(FinanceFlowDirectionEnum.IN.getCode());
        flow.setChangeType(OwnerAccountFlowChangeTypeEnum.BILL_SETTLE_IN.getCode());
        flow.setAmount(amount);
        flow.setAvailableBefore(availableBefore);
        flow.setAvailableAfter(account.getAvailableAmount());
        flow.setFrozenBefore(frozenBefore);
        flow.setFrozenAfter(account.getFrozenAmount());
        flow.setRemark("起租日账单入账");
        flow.setCreateBy(contract.getCreateBy());
        flow.setCreateTime(now);
        ownerAccountFlowRepo.save(flow);
    }

    private String generateOwnerBillNo() {
        return "OWB" + IdUtil.getSnowflakeNextIdStr();
    }
}
