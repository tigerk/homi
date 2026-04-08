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
import com.homi.common.lib.enums.owner.*;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class OwnerBillService {
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final OwnerSettlementRuleRepo ownerSettlementRuleRepo;
    private final OwnerRentFreeRuleRepo ownerRentFreeRuleRepo;
    private final OwnerBillRepo ownerBillRepo;
    private final OwnerBillLineRepo ownerBillLineRepo;
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
