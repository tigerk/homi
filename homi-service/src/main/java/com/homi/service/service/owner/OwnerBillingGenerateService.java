package com.homi.service.service.owner;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.lease.LeaseBillFeeTypeEnum;
import com.homi.common.lib.enums.lease.LeaseRentDueTypeEnum;
import com.homi.common.lib.enums.owner.*;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
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
 * 业主单据生成服务
 * <p>
 * 负责轻托管结算单和包租应付单的生成。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OwnerBillingGenerateService {
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final OwnerSettlementRuleRepo ownerSettlementRuleRepo;
    private final OwnerSettlementFeeRepo ownerSettlementFeeRepo;
    private final OwnerRentFreeRuleRepo ownerRentFreeRuleRepo;
    private final OwnerLeaseRuleRepo ownerLeaseRuleRepo;
    private final OwnerLeaseFeeRepo ownerLeaseFeeRepo;
    private final OwnerLeaseFreeRuleRepo ownerLeaseFreeRuleRepo;
    private final OwnerSettlementBillRepo ownerSettlementBillRepo;
    private final OwnerSettlementBillFeeRepo ownerSettlementBillFeeRepo;
    private final OwnerSettlementBillReductionRepo ownerSettlementBillReductionRepo;
    private final OwnerPayableBillRepo ownerPayableBillRepo;
    private final OwnerPayableBillFeeRepo ownerPayableBillFeeRepo;
    private final OwnerPayableBillPaymentRepo ownerPayableBillPaymentRepo;
    private final OwnerAccountRepo ownerAccountRepo;
    private final OwnerAccountFlowRepo ownerAccountFlowRepo;
    private final LeaseRepo leaseRepo;
    private final LeaseRoomRepo leaseRoomRepo;
    private final RoomRepo roomRepo;

    /**
     * 自动生成起租日业主结算单
     *
     * @return 成功生成的账单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer generateLeaseStartSettlementBills() {
        Date todayStart = DateUtil.beginOfDay(new Date());
        Date todayEnd = DateUtil.endOfDay(new Date());

        List<OwnerContract> contractList = ownerContractRepo.list(new LambdaQueryWrapper<OwnerContract>()
            .eq(OwnerContract::getCooperationMode, OwnerCooperationModeEnum.LIGHT_MANAGED.name())
            .eq(OwnerContract::getStatus, StatusEnum.ACTIVE.getValue())
            .eq(OwnerContract::getApprovalStatus, BizApprovalStatusEnum.APPROVED.getCode())
            .le(OwnerContract::getContractStart, todayEnd));

        int successCount = 0;
        for (OwnerContract contract : contractList) {
            try {
                if (generateLeaseStartSettlementBillByContract(contract.getId(), todayStart, todayEnd)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("生成起租日业主账单失败, contractId={}", contract.getId(), e);
            }
        }
        return successCount;
    }

    /**
     * 租客支付成功后，按轻托管“租客支付实时分账”规则生成业主结算单。
     * <p>
     * 当前按“合同 + 房源 + 支付日期”聚合：
     * - 同一天同一房源多次收款，合并到同一张实时结算单
     * - 同一支付流水重复回调，通过支付流水来源明细防重
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateRealtimeSettlementBillByPaymentFlow(PaymentFlow paymentFlow, LeaseBill bill,
                                                            Map<Long, LeaseBillFee> feeMap, LeaseBillCollectDTO dto, Long operatorId, Date now) {
        if (paymentFlow == null || bill == null || feeMap == null || feeMap.isEmpty() || dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return;
        }

        Long houseId = resolveLeaseHouseId(bill.getLeaseId());
        if (houseId == null) {
            return;
        }

        OwnerContractSubject contractSubject = resolveRealtimeContractSubject(houseId);
        if (contractSubject == null) {
            return;
        }

        OwnerContract contract = ownerContractRepo.getById(contractSubject.getContractId());
        if (contract == null || !isLeaseStartBillContract(contract)) {
            return;
        }

        OwnerSettlementRule settlementRule = ownerSettlementRuleRepo.lambdaQuery()
            .eq(OwnerSettlementRule::getContractId, contract.getId())
            .eq(OwnerSettlementRule::getContractSubjectId, contractSubject.getId())
            .eq(OwnerSettlementRule::getStatus, StatusEnum.ACTIVE.getValue())
            .eq(OwnerSettlementRule::getSettlementTiming, OwnerSettlementTimingEnum.TENANT_PAYMENT_REALTIME.getCode())
            .orderByDesc(OwnerSettlementRule::getId)
            .last("limit 1")
            .one();
        if (settlementRule == null) {
            return;
        }

        Date billDate = DateUtil.beginOfDay(ObjectUtil.defaultIfNull(dto.getPayAt(), paymentFlow.getPayAt()));
        OwnerSettlementBill settlementBill = ownerSettlementBillRepo.lambdaQuery()
            .eq(OwnerSettlementBill::getContractId, contract.getId())
            .eq(OwnerSettlementBill::getSubjectId, contractSubject.getSubjectId())
            .eq(OwnerSettlementBill::getBillStartDate, billDate)
            .eq(OwnerSettlementBill::getBillEndDate, billDate)
            .orderByDesc(OwnerSettlementBill::getId)
            .last("limit 1")
            .one();

        Long existingBillId = settlementBill == null ? null : settlementBill.getId();
        if (existingBillId != null) {
            long existsCount = ownerSettlementBillFeeRepo.lambdaQuery()
                .eq(OwnerSettlementBillFee::getBillId, existingBillId)
                .eq(OwnerSettlementBillFee::getSourceType, OwnerBillingSourceTypeEnum.PAYMENT_FLOW.getCode())
                .eq(OwnerSettlementBillFee::getSourceId, paymentFlow.getId())
                .count();
            if (existsCount > 0) {
                return;
            }
        }

        List<OwnerSettlementFee> settlementFeeList = ownerSettlementFeeRepo.lambdaQuery()
            .eq(OwnerSettlementFee::getContractId, contract.getId())
            .eq(OwnerSettlementFee::getContractSubjectId, contractSubject.getId())
            .eq(OwnerSettlementFee::getStatus, StatusEnum.ACTIVE.getValue())
            .orderByAsc(OwnerSettlementFee::getSortOrder)
            .orderByAsc(OwnerSettlementFee::getId)
            .list();
        if (settlementFeeList.isEmpty()) {
            return;
        }

        RealtimeSettlementResult result = buildRealtimeSettlementResult(paymentFlow, dto, feeMap, settlementRule, settlementFeeList, contractSubject, billDate);
        if (result.feeList.isEmpty()) {
            return;
        }

        boolean created = settlementBill == null;
        BigDecimal previousWithdrawable = created
            ? BigDecimal.ZERO
            : ObjectUtil.defaultIfNull(settlementBill.getWithdrawableAmount(), BigDecimal.ZERO);

        if (created) {
            settlementBill = createRealtimeSettlementBill(contract, contractSubject, billDate, now, operatorId);
        }

        settlementBill.setIncomeAmount(ObjectUtil.defaultIfNull(settlementBill.getIncomeAmount(), BigDecimal.ZERO).add(result.incomeAmount));
        settlementBill.setExpenseAmount(ObjectUtil.defaultIfNull(settlementBill.getExpenseAmount(), BigDecimal.ZERO).add(result.expenseAmount));
        settlementBill.setReductionAmount(ObjectUtil.defaultIfNull(settlementBill.getReductionAmount(), BigDecimal.ZERO));
        settlementBill.setAdjustAmount(ObjectUtil.defaultIfNull(settlementBill.getAdjustAmount(), BigDecimal.ZERO));
        BigDecimal payableAmount = ObjectUtil.defaultIfNull(settlementBill.getIncomeAmount(), BigDecimal.ZERO)
            .subtract(ObjectUtil.defaultIfNull(settlementBill.getExpenseAmount(), BigDecimal.ZERO))
            .subtract(ObjectUtil.defaultIfNull(settlementBill.getReductionAmount(), BigDecimal.ZERO))
            .add(ObjectUtil.defaultIfNull(settlementBill.getAdjustAmount(), BigDecimal.ZERO));
        settlementBill.setPayableAmount(payableAmount);
        settlementBill.setWithdrawableAmount(resolveWithdrawableAmount(settlementBill));
        settlementBill.setUpdateBy(operatorId);
        settlementBill.setUpdateAt(now);
        if (created) {
            ownerSettlementBillRepo.save(settlementBill);
        } else {
            ownerSettlementBillRepo.updateById(settlementBill);
        }

        Date createAt = ObjectUtil.defaultIfNull(now, new Date());
        Long settlementBillId = settlementBill.getId();
        Long companyId = contract.getCompanyId();
        result.feeList.forEach(item -> {
            item.setBillId(settlementBillId);
            item.setCompanyId(companyId);
            item.setCreateAt(createAt);
        });
        ownerSettlementBillFeeRepo.saveBatch(result.feeList);

        BigDecimal currentWithdrawable = ObjectUtil.defaultIfNull(settlementBill.getWithdrawableAmount(), BigDecimal.ZERO);
        BigDecimal delta = currentWithdrawable.subtract(previousWithdrawable);
        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            adjustOwnerAccountAmount(contract, settlementBill, delta, now);
        }
    }

    /**
     * 重建包租合同的全部业主应付单计划。
     * <p>
     * 在新增或编辑包租合同后立即调用，直接按合同周期生成完整账单。
     *
     * @param contractId 合同ID
     * @return 成功生成的账单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer rebuildMasterLeasePayableBillsByContract(Long contractId) {
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
        return generateMasterLeasePayableBillsByContract(contractId, planEnd);
    }

    /**
     * 清空包租合同下尚未发生结算的账单计划。
     *
     * @param contractId 合同ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearMasterLeasePayableBillsByContract(Long contractId) {
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
        List<OwnerPayableBill> billList = ownerPayableBillRepo.lambdaQuery()
            .eq(OwnerPayableBill::getContractId, contractId)
            .list();
        if (billList.isEmpty()) {
            return false;
        }
        boolean hasPaidBill = billList.stream().anyMatch(item ->
            ObjectUtil.defaultIfNull(item.getPaidAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
                || !Objects.equals(item.getPaymentStatus(), OwnerPayableBillPaymentStatusEnum.UNPAID.getCode())
        );
        if (hasPaidBill) {
            return true;
        }
        List<Long> billIds = billList.stream().map(OwnerPayableBill::getId).toList();
        return ownerPayableBillPaymentRepo.lambdaQuery()
            .in(OwnerPayableBillPayment::getBillId, billIds)
            .count() > 0;
    }

    /**
     * 按合同生成起租日业主结算单
     *
     * @param contractId 合同ID
     * @param todayStart 当天开始时间
     * @param todayEnd   当天结束时间
     * @return 是否成功生成账单
     */
    public boolean generateLeaseStartSettlementBillByContract(Long contractId, Date todayStart, Date todayEnd) {
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
            List<OwnerSettlementBillFee> feeList = new ArrayList<>();
            List<String> remarkList = new ArrayList<>();

            OwnerRentFreeRule rentFreeRule = rentFreeRuleMap.get(contractSubject.getId());
            if (isMatchedRentFreeRule(billDate, rentFreeRule)) {
                remarkList.add("合同房源【" + CharSequenceUtil.nullToDefault(contractSubject.getSubjectNameSnapshot(), "") + "】命中免租规则，当前未自动冲减，请人工复核。");
            }

            feeList.add(buildRentLine(contractSubject, settlementRule, billDate, rentAmount));

            BigDecimal managementFeeAmount = calcManagementFee(settlementRule, rentAmount);
            if (managementFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
                expenseAmount = expenseAmount.add(managementFeeAmount);
                feeList.add(buildManagementFeeLine(contractSubject, billDate, managementFeeAmount));
            }

            BigDecimal payableAmount = incomeAmount.subtract(expenseAmount);
            BigDecimal withdrawableAmount = payableAmount.compareTo(BigDecimal.ZERO) > 0 ? payableAmount : BigDecimal.ZERO;
            Date now = DateUtil.date();

            OwnerSettlementBill ownerBill = new OwnerSettlementBill();
            ownerBill.setCompanyId(contract.getCompanyId());
            ownerBill.setOwnerId(contract.getOwnerId());
            ownerBill.setContractId(contract.getId());
            ownerBill.setSubjectType(contractSubject.getSubjectType());
            ownerBill.setSubjectId(contractSubject.getSubjectId());
            ownerBill.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
            ownerBill.setBillNo(generateOwnerSettlementBillNo());
            ownerBill.setBillStartDate(billDate);
            ownerBill.setBillEndDate(billDate);
            ownerBill.setIncomeAmount(incomeAmount);
            ownerBill.setReductionAmount(BigDecimal.ZERO);
            ownerBill.setExpenseAmount(expenseAmount);
            ownerBill.setAdjustAmount(BigDecimal.ZERO);
            ownerBill.setPayableAmount(payableAmount);
            ownerBill.setSettledAmount(BigDecimal.ZERO);
            ownerBill.setWithdrawnAmount(BigDecimal.ZERO);
            ownerBill.setFreezeAmount(BigDecimal.ZERO);
            ownerBill.setWithdrawableAmount(withdrawableAmount);
            ownerBill.setBillStatus(OwnerSettlementBillStatusEnum.NORMAL.getCode());
            ownerBill.setApprovalStatus(BizApprovalStatusEnum.APPROVED.getCode());
            ownerBill.setSettlementStatus(OwnerSettlementStatusEnum.UNSETTLED.getCode());
            ownerBill.setGeneratedAt(now);
            ownerBill.setApprovedAt(now);
            ownerBill.setRemark(remarkList.isEmpty() ? "起租日自动生成账单" : String.join("；", remarkList));
            ownerBill.setCreateAt(now);
            ownerBill.setUpdateAt(now);
            ownerSettlementBillRepo.save(ownerBill);

            feeList.forEach(item -> {
                item.setBillId(ownerBill.getId());
                item.setCompanyId(contract.getCompanyId());
                item.setCreateAt(now);
            });
            ownerSettlementBillFeeRepo.saveBatch(feeList);

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

    private int generateMasterLeasePayableBillsByContract(Long contractId, Date todayEnd) {
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
        List<OwnerPayableBill> billList = ownerPayableBillRepo.lambdaQuery()
            .eq(OwnerPayableBill::getContractId, contractId)
            .orderByAsc(OwnerPayableBill::getId)
            .list();
        if (billList.isEmpty()) {
            return;
        }
        if (isMasterLeaseBillLocked(contractId)) {
            throw new IllegalArgumentException("包租账单已发生付款或结算，账单条款已锁定");
        }

        List<Long> billIds = billList.stream().map(OwnerPayableBill::getId).toList();
        ownerPayableBillFeeRepo.remove(new LambdaQueryWrapper<OwnerPayableBillFee>().in(OwnerPayableBillFee::getBillId, billIds));
        ownerPayableBillRepo.physicalDeleteByIds(billIds);
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
        return ownerPayableBillRepo.count(new LambdaQueryWrapper<OwnerPayableBill>()
            .eq(OwnerPayableBill::getContractId, contractId)
            .eq(OwnerPayableBill::getBillStartDate, billStart)
            .eq(OwnerPayableBill::getBillEndDate, billEnd)) > 0;
    }

    /**
     * 创建包租应付账单
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/20 09:24
     *
     * @param contract          业主合同信息
     * @param leaseRule         包租规则
     * @param leaseFeeList      其他费用列表
     * @param leaseFreeRuleList 包租优惠规则列表
     * @param periodStart       本期账单开始日期
     * @param periodEnd         本期账单结束日期
     * @param firstPeriod       是否是首期账单
     */
    private void createMasterLeaseBill(OwnerContract contract, OwnerLeaseRule leaseRule,
                                       List<OwnerLeaseFee> leaseFeeList, List<OwnerLeaseFreeRule> leaseFreeRuleList, Date periodStart, Date periodEnd, boolean firstPeriod) {
        Date now = new Date();
        List<OwnerContractSubject> subjectList = ownerContractSubjectRepo.listByContractId(contract.getId());
        String subjectSummary = buildMasterLeaseSubjectSummary(subjectList);

        BigDecimal incomeAmount = BigDecimal.ZERO;
        BigDecimal expenseAmount = BigDecimal.ZERO;
        BigDecimal reductionAmount = BigDecimal.ZERO;
        List<OwnerPayableBillFee> feeList = new ArrayList<>();

        BigDecimal rentAmount = calcMasterLeaseRentAmount(leaseRule, periodStart, periodEnd);
        if (rentAmount.compareTo(BigDecimal.ZERO) > 0) {
            incomeAmount = incomeAmount.add(rentAmount);
            feeList.add(buildMasterLeaseBillFee(contract, periodStart, OwnerBillingSourceTypeEnum.OWNER_CONTRACT.getCode(), contract.getId(),
                OwnerBillingItemTypeEnum.RENT.getCode(), null, OwnerBillingItemTypeEnum.RENT.getName(), FinanceFlowDirectionEnum.IN.getCode(), rentAmount,
                "包租周期租金", "月租金 " + ObjectUtil.defaultIfNull(leaseRule.getRentAmount(), BigDecimal.ZERO) + "，按账期自动生成"));
        }

        if (firstPeriod && ObjectUtil.defaultIfNull(leaseRule.getDepositAmount(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal depositAmount = ObjectUtil.defaultIfNull(leaseRule.getDepositAmount(), BigDecimal.ZERO);
            incomeAmount = incomeAmount.add(depositAmount);
            feeList.add(buildMasterLeaseBillFee(contract, periodStart, OwnerBillingSourceTypeEnum.OWNER_CONTRACT.getCode(), contract.getId(),
                OwnerBillingItemTypeEnum.DEPOSIT.getCode(), null, OwnerBillingItemTypeEnum.DEPOSIT.getName(), FinanceFlowDirectionEnum.IN.getCode(), depositAmount,
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
            feeList.add(buildMasterLeaseBillFee(contract, periodStart, OwnerBillingSourceTypeEnum.OWNER_LEASE_FEE.getCode(), leaseFee.getId(),
                OwnerBillingItemTypeEnum.OTHER_FEE.getCode(), leaseFee.getDictDataId(), ObjectUtil.defaultIfNull(leaseFee.getFeeName(), "其他费用"), direction, feeAmount,
                leaseFee.getRemark(), buildMasterLeaseFeeFormula(leaseRule, leaseFee, periodStart, periodEnd)));
        }

        for (OwnerLeaseFreeRule freeRule : leaseFreeRuleList) {
            BigDecimal currentReductionAmount = calcMasterLeaseReductionAmount(rentAmount, freeRule, periodStart, periodEnd);
            if (currentReductionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            reductionAmount = reductionAmount.add(currentReductionAmount);
            feeList.add(buildMasterLeaseBillFee(contract, periodStart, OwnerBillingSourceTypeEnum.OWNER_LEASE_FREE_RULE.getCode(), freeRule.getId(),
                OwnerBillingItemTypeEnum.OTHER_FEE.getCode(), null, "包租免租", FinanceFlowDirectionEnum.OUT.getCode(), currentReductionAmount,
                freeRule.getRemark(), "calcMode=" + freeRule.getCalcMode()));
        }

        BigDecimal payableAmount = incomeAmount.subtract(expenseAmount).subtract(reductionAmount);

        OwnerPayableBill ownerBill = new OwnerPayableBill();
        ownerBill.setCompanyId(contract.getCompanyId());
        ownerBill.setOwnerId(contract.getOwnerId());
        ownerBill.setContractId(contract.getId());
        ownerBill.setSubjectNameSnapshot(subjectSummary);
        ownerBill.setBillNo(generateOwnerPayableBillNo());
        ownerBill.setBillStartDate(periodStart);
        ownerBill.setBillEndDate(periodEnd);
        ownerBill.setDueDate(resolveMasterLeaseDueDate(leaseRule, periodStart));
        ownerBill.setAdjustAmount(BigDecimal.ZERO);
        ownerBill.setPayableAmount(payableAmount);
        ownerBill.setPaidAmount(BigDecimal.ZERO);
        ownerBill.setUnpaidAmount(payableAmount);
        ownerBill.setBillStatus(OwnerPayableBillStatusEnum.NORMAL.getCode());
        ownerBill.setPaymentStatus(OwnerPayableBillPaymentStatusEnum.UNPAID.getCode());
        ownerBill.setGeneratedAt(now);
        ownerBill.setRemark("包租应付账单自动生成");
        ownerBill.setCreateAt(now);
        ownerBill.setUpdateAt(now);
        ownerPayableBillRepo.save(ownerBill);

        feeList.forEach(item -> {
            item.setBillId(ownerBill.getId());
            item.setCompanyId(contract.getCompanyId());
            item.setSubjectNameSnapshot(subjectSummary);
            item.setCreateAt(now);
        });
        if (!feeList.isEmpty()) {
            ownerPayableBillFeeRepo.saveBatch(feeList);
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

    private OwnerPayableBillFee buildMasterLeaseBillFee(
        OwnerContract contract,
        Date bizDate,
        String sourceType,
        Long sourceId,
        String feeType,
        Long dictDataId,
        String feeName,
        String direction,
        BigDecimal amount,
        String remark,
        String formulaSnapshot
    ) {
        OwnerPayableBillFee line = new OwnerPayableBillFee();
        line.setSourceType(sourceType);
        line.setSourceId(sourceId);
        line.setFeeType(feeType);
        line.setDictDataId(dictDataId);
        line.setFeeName(feeName);
        line.setDirection(direction);
        line.setAmount(amount);
        line.setBizDate(bizDate);
        line.setRemark(remark);
        line.setFormulaSnapshot(formulaSnapshot);
        return line;
    }

    private Long resolveLeaseHouseId(Long leaseId) {
        if (leaseId == null) {
            return null;
        }
        List<Long> roomIds = leaseRoomRepo.getListByLeaseId(leaseId).stream()
            .map(LeaseRoom::getRoomId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (roomIds.isEmpty()) {
            Lease lease = leaseRepo.getById(leaseId);
            if (lease != null && StrUtil.isNotBlank(lease.getRoomIds())) {
                roomIds = JSONUtil.toList(lease.getRoomIds(), Long.class).stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            }
        }
        if (roomIds.isEmpty()) {
            return null;
        }
        Set<Long> houseIds = roomRepo.listByIds(roomIds).stream()
            .map(Room::getHouseId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (houseIds.isEmpty()) {
            return null;
        }
        if (houseIds.size() > 1) {
            log.warn("租约关联多个房源，实时分账暂不自动处理, leaseId={}, houseIds={}", leaseId, houseIds);
            return null;
        }
        return houseIds.iterator().next();
    }

    private OwnerContractSubject resolveRealtimeContractSubject(Long houseId) {
        List<OwnerContractSubject> subjectList = ownerContractSubjectRepo.lambdaQuery()
            .eq(OwnerContractSubject::getSubjectType, OwnerContractSubjectTypeEnum.HOUSE.getCode())
            .eq(OwnerContractSubject::getSubjectId, houseId)
            .eq(OwnerContractSubject::getStatus, StatusEnum.ACTIVE.getValue())
            .list();
        if (subjectList.isEmpty()) {
            return null;
        }
        Set<Long> contractIds = subjectList.stream().map(OwnerContractSubject::getContractId).collect(Collectors.toSet());
        Map<Long, OwnerContract> contractMap = ownerContractRepo.listByIds(contractIds).stream()
            .filter(this::isLeaseStartBillContract)
            .collect(Collectors.toMap(OwnerContract::getId, Function.identity(), (left, right) -> right));
        return subjectList.stream()
            .filter(item -> contractMap.containsKey(item.getContractId()))
            .max(Comparator.comparing(OwnerContractSubject::getId))
            .orElse(null);
    }

    private RealtimeSettlementResult buildRealtimeSettlementResult(
        PaymentFlow paymentFlow,
        LeaseBillCollectDTO dto,
        Map<Long, LeaseBillFee> feeMap,
        OwnerSettlementRule settlementRule,
        List<OwnerSettlementFee> settlementFeeList,
        OwnerContractSubject contractSubject,
        Date billDate
    ) {
        List<OwnerSettlementBillFee> feeList = new ArrayList<>();
        BigDecimal incomeAmount = BigDecimal.ZERO;
        BigDecimal expenseAmount = BigDecimal.ZERO;
        BigDecimal managementBaseAmount = BigDecimal.ZERO;

        for (LeaseBillCollectDTO.Item item : dto.getItems()) {
            LeaseBillFee leaseBillFee = feeMap.get(item.getLeaseBillFeeId());
            BigDecimal collectAmount = ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO);
            if (leaseBillFee == null || collectAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            OwnerSettlementFee settlementFee = matchSettlementFeeRule(leaseBillFee, settlementFeeList);
            if (settlementFee == null || !Boolean.TRUE.equals(settlementFee.getTransferEnabled())) {
                continue;
            }

            BigDecimal transferRatio = normalizeTransferRatio(settlementFee.getTransferRatio());
            BigDecimal ownerAmount = collectAmount.multiply(transferRatio)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (ownerAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            String direction = normalizeDirection(settlementFee.getFeeDirection());
            if (FinanceFlowDirectionEnum.OUT.getCode().equals(direction)) {
                expenseAmount = expenseAmount.add(ownerAmount);
            } else {
                incomeAmount = incomeAmount.add(ownerAmount);
                if (LeaseBillFeeTypeEnum.RENTAL.getCode().equals(leaseBillFee.getFeeType())) {
                    managementBaseAmount = managementBaseAmount.add(ownerAmount);
                }
            }

            OwnerSettlementBillFee billFee = new OwnerSettlementBillFee();
            billFee.setSourceType(OwnerBillingSourceTypeEnum.PAYMENT_FLOW.getCode());
            billFee.setSourceId(paymentFlow.getId());
            billFee.setSubjectType(contractSubject.getSubjectType());
            billFee.setSubjectId(contractSubject.getSubjectId());
            billFee.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
            billFee.setFeeType(ObjectUtil.defaultIfNull(settlementFee.getFeeType(), leaseBillFee.getFeeType()));
            billFee.setDictDataId(ObjectUtil.defaultIfNull(settlementFee.getDictDataId(), leaseBillFee.getDictDataId()));
            billFee.setFeeName(resolveRealtimeFeeName(settlementFee, leaseBillFee));
            billFee.setDirection(direction);
            billFee.setAmount(ownerAmount);
            billFee.setBizDate(billDate);
            billFee.setRemark(settlementFee.getRemark());
            billFee.setFormulaSnapshot(buildRealtimeFormula(paymentFlow, leaseBillFee, collectAmount, transferRatio));
            feeList.add(billFee);
        }

        BigDecimal managementFeeAmount = calcManagementFee(settlementRule, managementBaseAmount);
        if (managementFeeAmount.compareTo(BigDecimal.ZERO) > 0) {
            expenseAmount = expenseAmount.add(managementFeeAmount);
            feeList.add(buildRealtimeManagementFeeLine(contractSubject, paymentFlow, billDate, managementFeeAmount));
        }

        return new RealtimeSettlementResult(feeList, incomeAmount, expenseAmount);
    }

    private OwnerSettlementFee matchSettlementFeeRule(LeaseBillFee leaseBillFee, List<OwnerSettlementFee> settlementFeeList) {
        if (leaseBillFee.getDictDataId() != null) {
            OwnerSettlementFee matched = settlementFeeList.stream()
                .filter(item -> Objects.equals(item.getDictDataId(), leaseBillFee.getDictDataId()))
                .findFirst()
                .orElse(null);
            if (matched != null) {
                return matched;
            }
        }
        return settlementFeeList.stream()
            .filter(item -> StrUtil.equals(item.getFeeType(), leaseBillFee.getFeeType()))
            .findFirst()
            .orElse(null);
    }

    private BigDecimal normalizeTransferRatio(BigDecimal transferRatio) {
        BigDecimal ratio = ObjectUtil.defaultIfNull(transferRatio, BigDecimal.valueOf(100));
        if (ratio.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (ratio.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(100);
        }
        return ratio;
    }

    private String normalizeDirection(String direction) {
        return FinanceFlowDirectionEnum.OUT.getCode().equals(direction)
            ? FinanceFlowDirectionEnum.OUT.getCode()
            : FinanceFlowDirectionEnum.IN.getCode();
    }

    private String resolveRealtimeFeeName(OwnerSettlementFee settlementFee, LeaseBillFee leaseBillFee) {
        if (StrUtil.isNotBlank(settlementFee.getFeeName())) {
            return settlementFee.getFeeName();
        }
        if (StrUtil.isNotBlank(leaseBillFee.getFeeName())) {
            return leaseBillFee.getFeeName();
        }
        return LeaseBillFeeTypeEnum.getLabelByCode(leaseBillFee.getFeeType());
    }

    private String buildRealtimeFormula(PaymentFlow paymentFlow, LeaseBillFee leaseBillFee, BigDecimal collectAmount, BigDecimal transferRatio) {
        return "paymentNo=" + ObjectUtil.defaultIfNull(paymentFlow.getPaymentNo(), "-")
            + ", leaseBillFeeId=" + leaseBillFee.getId()
            + ", collectAmount=" + collectAmount
            + ", transferRatio=" + transferRatio + "%";
    }

    private OwnerSettlementBill createRealtimeSettlementBill(
        OwnerContract contract,
        OwnerContractSubject contractSubject,
        Date billDate,
        Date now,
        Long operatorId
    ) {
        OwnerSettlementBill bill = new OwnerSettlementBill();
        bill.setCompanyId(contract.getCompanyId());
        bill.setOwnerId(contract.getOwnerId());
        bill.setContractId(contract.getId());
        bill.setSubjectType(contractSubject.getSubjectType());
        bill.setSubjectId(contractSubject.getSubjectId());
        bill.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
        bill.setBillNo(generateOwnerSettlementBillNo());
        bill.setBillStartDate(billDate);
        bill.setBillEndDate(billDate);
        bill.setIncomeAmount(BigDecimal.ZERO);
        bill.setReductionAmount(BigDecimal.ZERO);
        bill.setExpenseAmount(BigDecimal.ZERO);
        bill.setAdjustAmount(BigDecimal.ZERO);
        bill.setPayableAmount(BigDecimal.ZERO);
        bill.setSettledAmount(BigDecimal.ZERO);
        bill.setWithdrawnAmount(BigDecimal.ZERO);
        bill.setFreezeAmount(BigDecimal.ZERO);
        bill.setWithdrawableAmount(BigDecimal.ZERO);
        bill.setBillStatus(OwnerSettlementBillStatusEnum.NORMAL.getCode());
        bill.setApprovalStatus(BizApprovalStatusEnum.APPROVED.getCode());
        bill.setSettlementStatus(OwnerSettlementStatusEnum.UNSETTLED.getCode());
        bill.setGeneratedAt(now);
        bill.setApprovedAt(now);
        bill.setRemark("租客支付实时分账");
        bill.setCreateBy(operatorId);
        bill.setCreateAt(now);
        bill.setUpdateBy(operatorId);
        bill.setUpdateAt(now);
        return bill;
    }

    private OwnerSettlementBillFee buildRealtimeManagementFeeLine(
        OwnerContractSubject contractSubject,
        PaymentFlow paymentFlow,
        Date billDate,
        BigDecimal amount
    ) {
        OwnerSettlementBillFee line = new OwnerSettlementBillFee();
        line.setSourceType(OwnerBillingSourceTypeEnum.PAYMENT_FLOW.getCode());
        line.setSourceId(paymentFlow.getId());
        line.setSubjectType(contractSubject.getSubjectType());
        line.setSubjectId(contractSubject.getSubjectId());
        line.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
        line.setFeeType(OwnerBillingItemTypeEnum.MANAGEMENT_FEE.getCode());
        line.setFeeName("管理费");
        line.setDirection(FinanceFlowDirectionEnum.OUT.getCode());
        line.setAmount(amount);
        line.setBizDate(billDate);
        line.setRemark("租客支付实时分账");
        line.setFormulaSnapshot("managementFee");
        return line;
    }

    private BigDecimal resolveWithdrawableAmount(OwnerSettlementBill bill) {
        BigDecimal payableAmount = ObjectUtil.defaultIfNull(bill.getPayableAmount(), BigDecimal.ZERO);
        BigDecimal withdrawnAmount = ObjectUtil.defaultIfNull(bill.getWithdrawnAmount(), BigDecimal.ZERO);
        BigDecimal freezeAmount = ObjectUtil.defaultIfNull(bill.getFreezeAmount(), BigDecimal.ZERO);
        BigDecimal withdrawable = payableAmount.subtract(withdrawnAmount).subtract(freezeAmount);
        return withdrawable.compareTo(BigDecimal.ZERO) > 0 ? withdrawable : BigDecimal.ZERO;
    }

    private boolean existsLeaseStartBill(Long contractId, Long subjectId, Date billDate) {
        return ownerSettlementBillRepo.count(new LambdaQueryWrapper<OwnerSettlementBill>()
            .eq(OwnerSettlementBill::getContractId, contractId)
            .eq(OwnerSettlementBill::getSubjectId, subjectId)
            .eq(OwnerSettlementBill::getBillStartDate, billDate)
            .eq(OwnerSettlementBill::getBillEndDate, billDate)) > 0;
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

    private OwnerSettlementBillFee buildRentLine(OwnerContractSubject contractSubject, OwnerSettlementRule settlementRule, Date billDate, BigDecimal amount) {
        OwnerSettlementBillFee line = new OwnerSettlementBillFee();
        line.setSourceType(OwnerBillingSourceTypeEnum.OWNER_CONTRACT_SUBJECT.getCode());
        line.setSourceId(contractSubject.getId());
        line.setCompanyId(null);
        line.setSubjectType(contractSubject.getSubjectType());
        line.setSubjectId(contractSubject.getSubjectId());
        line.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
        line.setFeeType(OwnerBillingItemTypeEnum.RENT.getCode());
        line.setFeeName(buildRentLineName(settlementRule));
        line.setDirection(FinanceFlowDirectionEnum.IN.getCode());
        line.setAmount(amount);
        line.setBizDate(billDate);
        line.setRemark(contractSubject.getSubjectNameSnapshot());
        line.setFormulaSnapshot("leaseStartBill");
        return line;
    }

    private OwnerSettlementBillFee buildManagementFeeLine(OwnerContractSubject contractSubject, Date billDate, BigDecimal amount) {
        OwnerSettlementBillFee line = new OwnerSettlementBillFee();
        line.setSourceType(OwnerBillingSourceTypeEnum.OWNER_CONTRACT_SUBJECT.getCode());
        line.setSourceId(contractSubject.getId());
        line.setCompanyId(null);
        line.setSubjectType(contractSubject.getSubjectType());
        line.setSubjectId(contractSubject.getSubjectId());
        line.setSubjectNameSnapshot(contractSubject.getSubjectNameSnapshot());
        line.setFeeType(OwnerBillingItemTypeEnum.MANAGEMENT_FEE.getCode());
        line.setFeeName("管理费");
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

    private void increaseOwnerAccountAmount(OwnerContract contract, OwnerSettlementBill ownerBill, BigDecimal amount, Date now) {
        adjustOwnerAccountAmount(contract, ownerBill, amount, now);
    }

    private void adjustOwnerAccountAmount(OwnerContract contract, OwnerSettlementBill ownerBill, BigDecimal amount, Date now) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        OwnerAccount account = ownerAccountRepo.getByOwnerId(contract.getOwnerId());
        if (account == null) {
            log.warn("业主账户不存在，跳过账户入账, ownerId={}, contractId={}", contract.getOwnerId(), contract.getId());
            return;
        }

        BigDecimal availableBefore = ObjectUtil.defaultIfNull(account.getAvailableAmount(), BigDecimal.ZERO);
        BigDecimal frozenBefore = ObjectUtil.defaultIfNull(account.getFrozenAmount(), BigDecimal.ZERO);
        account.setAvailableAmount(availableBefore.add(amount));
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            account.setTotalIncomeAmount(ObjectUtil.defaultIfNull(account.getTotalIncomeAmount(), BigDecimal.ZERO).add(amount));
        } else {
            account.setTotalReductionAmount(ObjectUtil.defaultIfNull(account.getTotalReductionAmount(), BigDecimal.ZERO).add(amount.abs()));
        }
        account.setUpdateAt(now);
        ownerAccountRepo.updateById(account);

        OwnerAccountFlow flow = new OwnerAccountFlow();
        flow.setCompanyId(contract.getCompanyId());
        flow.setOwnerId(contract.getOwnerId());
        flow.setBizType(OwnerAccountFlowBizTypeEnum.OWNER_BILL.getCode());
        flow.setBizId(ownerBill.getId());
        flow.setFlowDirection(amount.compareTo(BigDecimal.ZERO) > 0 ? FinanceFlowDirectionEnum.IN.getCode() : FinanceFlowDirectionEnum.OUT.getCode());
        flow.setChangeType(amount.compareTo(BigDecimal.ZERO) > 0
            ? OwnerAccountFlowChangeTypeEnum.BILL_SETTLE_IN.getCode()
            : OwnerAccountFlowChangeTypeEnum.BILL_SETTLE_OUT.getCode());
        flow.setAmount(amount.abs());
        flow.setAvailableBefore(availableBefore);
        flow.setAvailableAfter(account.getAvailableAmount());
        flow.setFrozenBefore(frozenBefore);
        flow.setFrozenAfter(account.getFrozenAmount());
        flow.setRemark(amount.compareTo(BigDecimal.ZERO) > 0 ? "业主结算账单入账" : "业主结算账单冲减");
        flow.setCreateBy(contract.getCreateBy());
        flow.setCreateAt(now);
        ownerAccountFlowRepo.save(flow);
    }

    private String generateOwnerSettlementBillNo() {
        return "OSB" + IdUtil.getSnowflakeNextIdStr();
    }

    private String generateOwnerPayableBillNo() {
        return "OPB" + IdUtil.getSnowflakeNextIdStr();
    }

    private record RealtimeSettlementResult(
        List<OwnerSettlementBillFee> feeList,
        BigDecimal incomeAmount,
        BigDecimal expenseAmount
    ) {
    }
}
