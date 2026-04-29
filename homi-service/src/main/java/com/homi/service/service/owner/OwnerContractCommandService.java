package com.homi.service.service.owner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.annotation.BizOperateLog;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;
import com.homi.common.lib.enums.contract.OwnerParamsEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.owner.*;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.owner.dto.*;
import com.homi.model.owner.vo.OwnerDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerContractCommandService {
    private final OwnerRepo ownerRepo;
    private final OwnerPersonalRepo ownerPersonalRepo;
    private final OwnerCompanyRepo ownerCompanyRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final OwnerSettlementRuleRepo ownerSettlementRuleRepo;
    private final OwnerSettlementFeeRepo ownerSettlementFeeRepo;
    private final OwnerRentFreeRuleRepo ownerRentFreeRuleRepo;
    private final OwnerLeaseRuleRepo ownerLeaseRuleRepo;
    private final OwnerLeaseFeeRepo ownerLeaseFeeRepo;
    private final OwnerLeaseFreeRuleRepo ownerLeaseFreeRuleRepo;
    private final OwnerPayableBillRepo ownerPayableBillRepo;
    private final OwnerSettlementBillRepo ownerSettlementBillRepo;
    private final OwnerAccountRepo ownerAccountRepo;
    private final ContractTemplateRepo contractTemplateRepo;
    private final HouseRepo houseRepo;
    private final RoomRepo roomRepo;
    private final LeaseRepo leaseRepo;
    private final FocusRepo focusRepo;
    private final FocusBuildingRepo focusBuildingRepo;
    private final FileAttachRepo fileAttachRepo;
    private final OwnerBillingGenerateService ownerBillingGenerateService;
    private final OwnerContractQueryService ownerContractQueryService;

    @Transactional(rollbackFor = Exception.class)
    public Long createOwnerContract(OwnerCreateDTO dto) {
        validateCreateDTO(dto);
        Long ownerId = saveOwner(dto);
        Date now = DateUtil.date();

        OwnerContract contract = BeanCopyUtils.copyBean(dto.getOwnerContract(), OwnerContract.class);
        assert contract != null;
        contract.setOwnerId(ownerId);
        contract.setContractNo(Objects.requireNonNullElseGet(contract.getContractNo(), this::generateContractNo));
        contract.setCooperationMode(enumName(dto.getOwnerContract().getCooperationMode()));
        contract.setSignStatus(Objects.requireNonNullElse(dto.getOwnerContract().getSignStatus(), OwnerSignStatusEnum.PENDING).getCode());
        contract.setSignType(enumName(dto.getOwnerContract().getSignType()));
        contract.setContractMedium(enumName(dto.getOwnerContract().getContractMedium()));
        contract.setNotifyOwner(Objects.requireNonNullElse(dto.getOwnerContract().getNotifyOwner(), Boolean.FALSE));
        contract.setStatus(Objects.requireNonNullElse(dto.getOwnerContract().getStatus(), StatusEnum.ACTIVE).getValue());
        contract.setApprovalStatus(Objects.requireNonNullElse(dto.getOwnerContract().getApprovalStatus(), BizApprovalStatusEnum.APPROVED).getCode());
        contract.setParentContractId(dto.getOwnerContract().getParentContractId());
        contract.setContractNature(Objects.requireNonNullElse(dto.getOwnerContract().getContractNature(), 1));
        contract.setRenewFromContractNo(dto.getOwnerContract().getRenewFromContractNo());
        contract.setCheckoutStatus(Objects.requireNonNullElse(dto.getOwnerContract().getCheckoutStatus(), 0));
        contract.setCreateBy(dto.getCreateBy());
        contract.setCreateAt(now);
        contract.setUpdateBy(dto.getCreateBy());
        contract.setUpdateAt(now);
        contract.setContractContent(buildContractContent(contract, ownerId, dto.getContractSubjectList()));
        ownerContractRepo.save(contract);

        List<OwnerContractSubject> contractSubjects = saveContractSubjects(dto, contract.getId(), now);
        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            saveLightManagedRules(dto, contract, contractSubjects, now);
        } else if (OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            saveMasterLeaseRules(dto, contract.getId(), now);
            ownerBillingGenerateService.rebuildMasterLeasePayableBillsByContract(contract.getId());
        }
        initOwnerAccount(dto.getOwnerContract().getCompanyId(), ownerId, now);
        return contract.getId();
    }

    @BizOperateLog(
        bizType = BizOperateBizTypeEnum.OWNER_CONTRACT,
        operateType = BizOperateTypeEnum.RENEW,
        operateDesc = "业主续约",
        bizIdExpr = "#result",
        remarkExpr = "#p0.ownerContract != null ? #p0.ownerContract.remark : null",
        sourceType = BizOperateSourceTypeEnum.OWNER_CONTRACT,
        sourceIdExpr = "#p0.sourceContractId",
        extraDataExpr = "{'sourceContractId': #p0.sourceContractId}"
    )
    @Transactional(rollbackFor = Exception.class)
    public Long renewOwnerContract(OwnerRenewDTO dto) {
        if (dto == null || dto.getSourceContractId() == null) {
            throw new IllegalArgumentException("续约来源合同ID不能为空");
        }
        validateCreateDTO(dto);
        OwnerContract source = ownerContractRepo.getById(dto.getSourceContractId());
        if (source == null) {
            throw new IllegalArgumentException("续约来源业主合同不存在");
        }
        if (Objects.equals(source.getCheckoutStatus(), 1)) {
            throw new IllegalArgumentException("已退房的业主合同不能续约");
        }
        Owner owner = ownerRepo.getById(source.getOwnerId());
        if (owner == null) {
            throw new IllegalArgumentException("业主不存在");
        }
        if (source.getContractEnd() != null && dto.getOwnerContract().getContractStart() != null
            && !dto.getOwnerContract().getContractStart().after(source.getContractEnd())) {
            throw new IllegalArgumentException("续约合同开始日期必须晚于原合同结束日期");
        }
        dto.getOwnerContract().setId(null);
        dto.getOwnerContract().setOwnerId(source.getOwnerId());
        dto.getOwnerContract().setCompanyId(source.getCompanyId());
        dto.getOwnerContract().setContractNo(null);
        dto.getOwnerContract().setParentContractId(source.getId());
        dto.getOwnerContract().setContractNature(2);
        dto.getOwnerContract().setRenewFromContractNo(source.getContractNo());
        dto.getOwnerContract().setCheckoutStatus(0);
        return createOwnerContractForExistingOwner(dto, source.getOwnerId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long updateOwnerContract(OwnerUpdateDTO dto) {
        validateUpdateDTO(dto);
        OwnerContract currentContract = ownerContractRepo.getById(dto.getOwnerContract().getId());
        if (currentContract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        Owner owner = ownerRepo.getById(currentContract.getOwnerId());
        if (owner == null) {
            throw new IllegalArgumentException("业主不存在");
        }
        updateOwnerInfo(dto, owner);

        Date now = DateUtil.date();
        // 是否需要重建包租合同账单的标志
        boolean shouldRebuildMasterLeaseBills = false;
        if (OwnerCooperationModeEnum.MASTER_LEASE.name().equals(currentContract.getCooperationMode())) {
            boolean masterLeaseBillLocked = ownerBillingGenerateService.isMasterLeaseBillLocked(currentContract.getId());
            boolean masterLeaseBillChanged = hasMasterLeaseBillChange(currentContract, dto);
            if (masterLeaseBillLocked && masterLeaseBillChanged) {
                throw new IllegalArgumentException("包租合同已发生付款或结算，账单条款已锁定；如需调整，请走合同变更");
            }
            if (!masterLeaseBillLocked && masterLeaseBillChanged) {
                ownerBillingGenerateService.clearMasterLeasePayableBillsByContract(currentContract.getId());
                shouldRebuildMasterLeaseBills = true;
            }
        }
        if (!OwnerCooperationModeEnum.MASTER_LEASE.name().equals(currentContract.getCooperationMode())
            && OwnerCooperationModeEnum.MASTER_LEASE.equals(dto.getOwnerContract().getCooperationMode())) {
            shouldRebuildMasterLeaseBills = true;
        }
        OwnerContract contract = BeanCopyUtils.copyBean(dto.getOwnerContract(), OwnerContract.class);
        assert contract != null;
        contract.setOwnerId(owner.getId());
        contract.setCooperationMode(enumName(dto.getOwnerContract().getCooperationMode()));
        contract.setSignStatus(Objects.requireNonNullElse(dto.getOwnerContract().getSignStatus(), OwnerSignStatusEnum.PENDING).getCode());
        contract.setSignType(enumName(dto.getOwnerContract().getSignType()));
        contract.setContractMedium(enumName(dto.getOwnerContract().getContractMedium()));
        contract.setNotifyOwner(Objects.requireNonNullElse(dto.getOwnerContract().getNotifyOwner(), Boolean.FALSE));
        contract.setStatus(Objects.requireNonNullElse(dto.getOwnerContract().getStatus(), StatusEnum.ACTIVE).getValue());
        contract.setApprovalStatus(Objects.requireNonNullElse(dto.getOwnerContract().getApprovalStatus(), BizApprovalStatusEnum.APPROVED).getCode());
        contract.setParentContractId(currentContract.getParentContractId());
        contract.setContractNature(Objects.requireNonNullElse(currentContract.getContractNature(), 1));
        contract.setRenewFromContractNo(currentContract.getRenewFromContractNo());
        contract.setCheckoutStatus(Objects.requireNonNullElse(currentContract.getCheckoutStatus(), 0));
        contract.setCheckoutDate(currentContract.getCheckoutDate());
        contract.setCheckoutReason(currentContract.getCheckoutReason());
        contract.setCheckoutBy(currentContract.getCheckoutBy());
        contract.setCheckoutByName(currentContract.getCheckoutByName());
        contract.setCheckoutAt(currentContract.getCheckoutAt());
        contract.setUpdateBy(dto.getUpdateBy());
        contract.setUpdateAt(now);
        contract.setContractContent(buildContractContent(contract, owner.getId(), dto.getContractSubjectList()));
        ownerContractRepo.updateById(contract);

        clearContractRelations(contract.getId());
        List<OwnerContractSubject> contractSubjects = saveContractSubjects(toCreateDTO(dto), contract.getId(), now);
        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            saveLightManagedRules(toCreateDTO(dto), contract, contractSubjects, now);
        } else {
            saveMasterLeaseRules(toCreateDTO(dto), contract.getId(), now);
            if (shouldRebuildMasterLeaseBills) {
                ownerBillingGenerateService.rebuildMasterLeasePayableBillsByContract(contract.getId());
            }
        }
        return contract.getId();
    }

    private Long createOwnerContractForExistingOwner(OwnerCreateDTO dto, Long ownerId) {
        Date now = DateUtil.date();
        OwnerContract contract = BeanCopyUtils.copyBean(dto.getOwnerContract(), OwnerContract.class);
        assert contract != null;
        contract.setId(null);
        contract.setOwnerId(ownerId);
        contract.setContractNo(Objects.requireNonNullElseGet(contract.getContractNo(), this::generateContractNo));
        contract.setCooperationMode(enumName(dto.getOwnerContract().getCooperationMode()));
        contract.setSignStatus(Objects.requireNonNullElse(dto.getOwnerContract().getSignStatus(), OwnerSignStatusEnum.PENDING).getCode());
        contract.setSignType(enumName(dto.getOwnerContract().getSignType()));
        contract.setContractMedium(enumName(dto.getOwnerContract().getContractMedium()));
        contract.setNotifyOwner(Objects.requireNonNullElse(dto.getOwnerContract().getNotifyOwner(), Boolean.FALSE));
        contract.setStatus(Objects.requireNonNullElse(dto.getOwnerContract().getStatus(), StatusEnum.ACTIVE).getValue());
        contract.setApprovalStatus(Objects.requireNonNullElse(dto.getOwnerContract().getApprovalStatus(), BizApprovalStatusEnum.APPROVED).getCode());
        contract.setContractNature(Objects.requireNonNullElse(dto.getOwnerContract().getContractNature(), 1));
        contract.setCheckoutStatus(Objects.requireNonNullElse(dto.getOwnerContract().getCheckoutStatus(), 0));
        contract.setCreateBy(dto.getCreateBy());
        contract.setCreateAt(now);
        contract.setUpdateBy(dto.getCreateBy());
        contract.setUpdateAt(now);
        contract.setContractContent(buildContractContent(contract, ownerId, dto.getContractSubjectList()));
        ownerContractRepo.save(contract);

        List<OwnerContractSubject> contractSubjects = saveContractSubjects(dto, contract.getId(), now);
        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            saveLightManagedRules(dto, contract, contractSubjects, now);
        } else if (OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            saveMasterLeaseRules(dto, contract.getId(), now);
            ownerBillingGenerateService.rebuildMasterLeasePayableBillsByContract(contract.getId());
        }
        initOwnerAccount(dto.getOwnerContract().getCompanyId(), ownerId, now);
        return contract.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long voidOwnerContract(OwnerContractVoidDTO dto, Long updateBy) {
        if (dto == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("合同ID不能为空");
        }
        if (CharSequenceUtil.isBlank(dto.getVoidReason())) {
            throw new IllegalArgumentException("作废原因不能为空");
        }
        OwnerContract contract = ownerContractRepo.getById(dto.getContractId());
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        validateOwnerContractCanVoid(contract);
        Date now = DateUtil.date();
        String reason = CharSequenceUtil.trim(dto.getVoidReason());
        contract.setVoidReason(reason);
        contract.setVoidBy(updateBy);
        contract.setVoidAt(now);
        contract.setUpdateBy(updateBy);
        contract.setUpdateAt(now);
        ownerContractRepo.updateById(contract);
        clearContractRelations(contract.getId());
        ownerContractRepo.removeById(contract.getId());
        return contract.getId();
    }

    /**
     * 校验业主合同是否仍处于可作废状态。
     * <p>
     * 作废只用于未进入业务流程的误建合同；已签约、已生成账单或已被租客占用的合同，必须走业主退房。
     */
    private void validateOwnerContractCanVoid(OwnerContract contract) {
        if (Objects.equals(contract.getSignStatus(), OwnerSignStatusEnum.SIGNED.getCode())) {
            throw new IllegalArgumentException("该合同已进入业务流程，请走业主退房");
        }
        if (Objects.equals(contract.getCheckoutStatus(), 1)) {
            throw new IllegalArgumentException("该合同已退房，不能作废");
        }
        if (hasOwnerContractBills(contract.getId()) || hasOwnerContractLeases(contract.getId())) {
            throw new IllegalArgumentException("该合同已进入业务流程，请走业主退房");
        }
    }

    /**
     * 判断业主合同是否已经产生任何业主账单。
     */
    private boolean hasOwnerContractBills(Long contractId) {
        return ownerPayableBillRepo.lambdaQuery().eq(OwnerPayableBill::getContractId, contractId).count() > 0
            || ownerSettlementBillRepo.lambdaQuery().eq(OwnerSettlementBill::getContractId, contractId).count() > 0;
    }

    /**
     * 判断业主合同覆盖房间是否已存在未退租租约。
     */
    private boolean hasOwnerContractLeases(Long contractId) {
        List<OwnerContractSubject> subjects = ownerContractSubjectRepo.listByContractId(contractId);
        List<Long> roomIds = resolveOwnerContractRoomIds(subjects);
        return CollUtil.isNotEmpty(roomIds) && CollUtil.isNotEmpty(leaseRepo.listOccupyingLeasesByRoomIds(roomIds));
    }

    /**
     * 将业主合同标的解析为房间ID列表，用于判断合同是否已经绑定租客业务。
     */
    private List<Long> resolveOwnerContractRoomIds(List<OwnerContractSubject> subjectList) {
        if (CollUtil.isEmpty(subjectList)) {
            return List.of();
        }
        LinkedHashSet<Long> houseIds = new LinkedHashSet<>();
        for (OwnerContractSubject subject : subjectList) {
            appendOwnerContractSubjectHouseIds(subject, houseIds);
        }
        if (houseIds.isEmpty()) {
            return List.of();
        }
        return roomRepo.lambdaQuery()
            .in(Room::getHouseId, houseIds)
            .list()
            .stream()
            .map(Room::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    /**
     * 根据业主合同标的类型追加房源ID，兼容整租房源、集中式项目和集中式楼栋。
     */
    private void appendOwnerContractSubjectHouseIds(OwnerContractSubject subject, Set<Long> houseIds) {
        if (subject == null || subject.getSubjectId() == null) {
            return;
        }
        OwnerContractSubjectTypeEnum subjectType = OwnerContractSubjectTypeEnum.fromCode(subject.getSubjectType());
        if (OwnerContractSubjectTypeEnum.HOUSE.equals(subjectType)) {
            houseIds.add(subject.getSubjectId());
            return;
        }
        if (OwnerContractSubjectTypeEnum.FOCUS.equals(subjectType)) {
            houseRepo.getHousesByLeaseModeId(subject.getSubjectId(), LeaseModeEnum.FOCUS.getCode())
                .stream()
                .map(House::getId)
                .filter(Objects::nonNull)
                .forEach(houseIds::add);
            return;
        }
        if (OwnerContractSubjectTypeEnum.FOCUS_BUILDING.equals(subjectType)) {
            appendFocusBuildingHouseIds(subject.getSubjectId(), houseIds);
        }
    }

    /**
     * 追加集中式楼栋下的房源ID。
     */
    private void appendFocusBuildingHouseIds(Long focusBuildingId, Set<Long> houseIds) {
        FocusBuilding focusBuilding = focusBuildingRepo.getById(focusBuildingId);
        if (focusBuilding == null) {
            return;
        }
        houseRepo.lambdaQuery()
            .eq(House::getLeaseMode, LeaseModeEnum.FOCUS.getCode())
            .eq(House::getLeaseModeId, focusBuilding.getFocusId())
            .eq(House::getBuilding, focusBuilding.getBuilding())
            .eq(House::getUnit, focusBuilding.getUnit())
            .list()
            .stream()
            .map(House::getId)
            .filter(Objects::nonNull)
            .forEach(houseIds::add);
    }

    private void validateCreateDTO(OwnerCreateDTO dto) {
        if (dto.getOwnerContract() == null) {
            throw new IllegalArgumentException("业主合同信息不能为空");
        }
        if (dto.getContractSubjectList() == null || dto.getContractSubjectList().isEmpty()) {
            throw new IllegalArgumentException("合同房源不能为空");
        }
        OwnerCooperationModeEnum mode = dto.getOwnerContract().getCooperationMode();
        if (mode == null) {
            throw new IllegalArgumentException("合作模式不正确");
        }
        if (OwnerCooperationModeEnum.MASTER_LEASE.equals(mode) && dto.getOwnerLeaseRule() == null) {
            throw new IllegalArgumentException("包租规则不能为空");
        }
    }

    private void validateUpdateDTO(OwnerUpdateDTO dto) {
        if (dto == null || dto.getOwnerContract() == null || dto.getOwnerContract().getId() == null) {
            throw new IllegalArgumentException("业主合同ID不能为空");
        }
        if (dto.getContractSubjectList() == null || dto.getContractSubjectList().isEmpty()) {
            throw new IllegalArgumentException("合同房源不能为空");
        }
        OwnerCooperationModeEnum mode = dto.getOwnerContract().getCooperationMode();
        if (mode == null) {
            throw new IllegalArgumentException("合作模式不正确");
        }
        if (OwnerCooperationModeEnum.MASTER_LEASE.equals(mode) && dto.getOwnerLeaseRule() == null) {
            throw new IllegalArgumentException("包租规则不能为空");
        }
    }

    private List<OwnerContractSubject> saveContractSubjects(OwnerCreateDTO dto, Long contractId, Date now) {
        List<OwnerContractSubject> records = dto.getContractSubjectList().stream().map(item -> {
            OwnerContractSubjectTypeEnum subjectType = Objects.requireNonNullElse(item.getSubjectType(), OwnerContractSubjectTypeEnum.HOUSE);
            if (item.getSubjectId() == null) {
                throw new IllegalArgumentException("合同房源ID不能为空");
            }
            OwnerContractSubject record = new OwnerContractSubject();
            record.setCompanyId(dto.getOwnerContract().getCompanyId());
            record.setContractId(contractId);
            record.setSubjectType(subjectType.getCode());
            record.setSubjectId(item.getSubjectId());
            record.setSubjectNameSnapshot(resolveSubjectName(subjectType, item.getSubjectId(), item.getSubjectName()));
            record.setRemark(item.getRemark());
            record.setStatus(1);
            record.setCreateBy(dto.getCreateBy());
            record.setCreateAt(now);
            record.setUpdateBy(dto.getCreateBy());
            record.setUpdateAt(now);
            return record;
        }).collect(Collectors.toList());
        ownerContractSubjectRepo.saveBatch(records);
        return records;
    }

    private void saveLightManagedRules(OwnerCreateDTO dto, OwnerContract contract, List<OwnerContractSubject> contractSubjects, Date now) {
        for (int i = 0; i < contractSubjects.size(); i++) {
            OwnerContractSubject subject = contractSubjects.get(i);
            OwnerContractSubjectDTO subjectDTO = dto.getContractSubjectList().get(i);
            OwnerSettlementRuleDTO settlementRuleDTO = subjectDTO.getSettlementRule();
            if (settlementRuleDTO != null) {
                OwnerSettlementRule rule = new OwnerSettlementRule();
                BeanUtils.copyProperties(settlementRuleDTO, rule);
                rule.setCompanyId(contract.getCompanyId());
                rule.setContractId(contract.getId());
                rule.setContractSubjectId(subject.getId());
                rule.setRuleVersion(1);
                rule.setIncomeBasis(enumName(settlementRuleDTO.getIncomeBasis()));
                rule.setSettlementMode(enumName(settlementRuleDTO.getSettlementMode()));
                rule.setHasGuaranteedRent(Objects.requireNonNullElse(settlementRuleDTO.getHasGuaranteedRent(), Boolean.FALSE));
                rule.setCommissionMode(enumName(settlementRuleDTO.getCommissionMode()));
                rule.setServiceFeeMode(enumName(settlementRuleDTO.getServiceFeeMode()));
                rule.setManagementFeeEnabled(Objects.requireNonNullElse(settlementRuleDTO.getManagementFeeEnabled(), Boolean.FALSE));
                rule.setManagementFeeMode(enumName(settlementRuleDTO.getManagementFeeMode()));
                rule.setManagementFeeValue(settlementRuleDTO.getManagementFeeValue());
                rule.setBearTaxType(enumName(settlementRuleDTO.getBearTaxType()));
                rule.setPaymentFeeBearType(enumName(settlementRuleDTO.getPaymentFeeBearType()));
                rule.setSettlementTiming(enumName(settlementRuleDTO.getSettlementTiming()));
                rule.setRentFreeEnabled(Objects.requireNonNullElse(settlementRuleDTO.getRentFreeEnabled(), Boolean.FALSE));
                rule.setStatus(Objects.requireNonNullElse(settlementRuleDTO.getStatus(), StatusEnum.ACTIVE).getValue());
                rule.setRuleSnapshot(JSONUtil.toJsonStr(settlementRuleDTO));
                rule.setCreateBy(dto.getCreateBy());
                rule.setCreateAt(now);
                rule.setUpdateBy(dto.getCreateBy());
                rule.setUpdateAt(now);
                ownerSettlementRuleRepo.save(rule);
                saveSettlementItems(dto, contract, subject, settlementRuleDTO.getSettlementItemList(), now);
            }
            OwnerRentFreeRuleDTO rentFreeRuleDTO = subjectDTO.getRentFreeRule();
            if (rentFreeRuleDTO != null) {
                OwnerRentFreeRule rule = new OwnerRentFreeRule();
                BeanUtils.copyProperties(rentFreeRuleDTO, rule);
                rule.setCompanyId(contract.getCompanyId());
                rule.setContractId(contract.getId());
                rule.setContractSubjectId(subject.getId());
                rule.setEnabled(Objects.requireNonNullElse(rentFreeRuleDTO.getEnabled(), Boolean.FALSE));
                rule.setFreeType(enumName(rentFreeRuleDTO.getFreeType()));
                rule.setBearType(enumName(rentFreeRuleDTO.getBearType()));
                rule.setCalcMode(enumName(rentFreeRuleDTO.getCalcMode()));
                rule.setStatus(Objects.requireNonNullElse(rentFreeRuleDTO.getStatus(), StatusEnum.ACTIVE).getValue());
                rule.setCreateBy(dto.getCreateBy());
                rule.setCreateAt(now);
                rule.setUpdateBy(dto.getCreateBy());
                rule.setUpdateAt(now);
                ownerRentFreeRuleRepo.save(rule);
            }
        }
    }

    private void saveMasterLeaseRules(OwnerCreateDTO dto, Long contractId, Date now) {
        OwnerLeaseRuleDTO leaseRuleDTO = dto.getOwnerLeaseRule();
        OwnerLeaseRule leaseRule = new OwnerLeaseRule();
        BeanUtils.copyProperties(leaseRuleDTO, leaseRule);
        leaseRule.setCompanyId(dto.getOwnerContract().getCompanyId());
        leaseRule.setContractId(contractId);
        leaseRule.setRentDueType(leaseRuleDTO.getRentDueType() == null ? null : leaseRuleDTO.getRentDueType().getCode());
        leaseRule.setProrateType(enumName(leaseRuleDTO.getProrateType()));
        leaseRule.setStatus(Objects.requireNonNullElse(leaseRuleDTO.getStatus(), StatusEnum.ACTIVE).getValue());
        leaseRule.setCreateBy(dto.getCreateBy());
        leaseRule.setCreateAt(now);
        leaseRule.setUpdateBy(dto.getCreateBy());
        leaseRule.setUpdateAt(now);
        ownerLeaseRuleRepo.save(leaseRule);
        saveLeaseFees(dto, contractId, now);

        if (dto.getOwnerLeaseFreeRuleList() == null || dto.getOwnerLeaseFreeRuleList().isEmpty()) {
            return;
        }
        List<OwnerLeaseFreeRule> freeRules = dto.getOwnerLeaseFreeRuleList().stream().map(item -> {
            OwnerLeaseFreeRule rule = new OwnerLeaseFreeRule();
            BeanUtils.copyProperties(item, rule);
            rule.setCompanyId(dto.getOwnerContract().getCompanyId());
            rule.setContractId(contractId);
            rule.setFreeType(enumName(item.getFreeType()));
            rule.setCalcMode(enumName(item.getCalcMode()));
            rule.setStatus(Objects.requireNonNullElse(item.getStatus(), StatusEnum.ACTIVE).getValue());
            rule.setCreateBy(dto.getCreateBy());
            rule.setCreateAt(now);
            rule.setUpdateBy(dto.getCreateBy());
            rule.setUpdateAt(now);
            return rule;
        }).toList();
        ownerLeaseFreeRuleRepo.saveBatch(freeRules);
    }

    private Long saveOwner(OwnerCreateDTO dto) {
        Date now = DateUtil.date();
        OwnerTypeEnum ownerType = dto.getOwnerType();
        if (ownerType == null) {
            throw new IllegalArgumentException("业主类型不能为空");
        }

        Long ownerTypeId;
        String ownerName;
        String ownerPhone;
        if (OwnerTypeEnum.PERSONAL.equals(ownerType)) {
            OwnerPersonalDTO personalDTO = dto.getOwnerPersonal();
            if (personalDTO == null) {
                throw new IllegalArgumentException("个人业主信息不能为空");
            }
            OwnerPersonal personal = new OwnerPersonal();
            BeanUtils.copyProperties(personalDTO, personal);
            personal.setCompanyId(dto.getOwnerContract().getCompanyId());
            personal.setGender(personalDTO.getGender() == null ? null : personalDTO.getGender().getCode());
            personal.setIdType(personalDTO.getIdType() == null ? null : personalDTO.getIdType().getCode());
            personal.setPayeeIdType(personalDTO.getPayeeIdType() == null ? null : personalDTO.getPayeeIdType().getCode());
            personal.setTags(JSONUtil.toJsonStr(personalDTO.getTags()));
            personal.setStatus(Objects.requireNonNullElse(personalDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            personal.setCreateBy(dto.getCreateBy());
            personal.setCreateAt(now);
            personal.setUpdateBy(dto.getCreateBy());
            personal.setUpdateAt(now);
            ownerPersonalRepo.save(personal);
            syncOwnerPersonalFiles(personal.getId(), personalDTO);
            ownerTypeId = personal.getId();
            ownerName = personal.getName();
            ownerPhone = personal.getPhone();
        } else {
            OwnerCompanyDTO companyDTO = dto.getOwnerCompany();
            if (companyDTO == null) {
                throw new IllegalArgumentException("企业业主信息不能为空");
            }
            OwnerCompany company = new OwnerCompany();
            BeanUtils.copyProperties(companyDTO, company);
            company.setCompanyId(dto.getOwnerContract().getCompanyId());
            company.setLegalPersonIdType(companyDTO.getLegalPersonIdType() == null ? null : companyDTO.getLegalPersonIdType().getCode());
            company.setPayeeIdType(companyDTO.getPayeeIdType() == null ? null : companyDTO.getPayeeIdType().getCode());
            company.setTags(JSONUtil.toJsonStr(companyDTO.getTags()));
            company.setStatus(Objects.requireNonNullElse(companyDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            company.setCreateBy(dto.getCreateBy());
            company.setCreateAt(now);
            company.setUpdateBy(dto.getCreateBy());
            company.setUpdateAt(now);
            ownerCompanyRepo.save(company);
            syncOwnerCompanyFiles(company.getId(), companyDTO);
            ownerTypeId = company.getId();
            ownerName = company.getName();
            ownerPhone = company.getContactPhone();
        }

        Owner owner = new Owner();
        owner.setCompanyId(dto.getOwnerContract().getCompanyId());
        owner.setOwnerType(ownerType.getCode());
        owner.setOwnerTypeId(ownerTypeId);
        owner.setOwnerName(ownerName);
        owner.setOwnerPhone(ownerPhone);
        owner.setStatus(StatusEnum.ACTIVE.getValue());
        owner.setCreateBy(dto.getCreateBy());
        owner.setCreateAt(now);
        owner.setUpdateBy(dto.getCreateBy());
        owner.setUpdateAt(now);
        ownerRepo.save(owner);
        return owner.getId();
    }

    private void updateOwnerInfo(OwnerUpdateDTO dto, Owner owner) {
        Date now = DateUtil.date();
        OwnerTypeEnum ownerType = dto.getOwnerType();
        if (ownerType == null) {
            throw new IllegalArgumentException("业主类型不能为空");
        }

        if (OwnerTypeEnum.PERSONAL.equals(ownerType)) {
            OwnerPersonal personal;
            if (OwnerTypeEnum.PERSONAL.equals(OwnerTypeEnum.fromCode(owner.getOwnerType())) && owner.getOwnerTypeId() != null) {
                personal = ownerPersonalRepo.getById(owner.getOwnerTypeId());
                if (personal == null) {
                    personal = new OwnerPersonal();
                }
            } else {
                personal = new OwnerPersonal();
                personal.setCompanyId(dto.getOwnerContract().getCompanyId());
                personal.setCreateBy(dto.getUpdateBy());
                personal.setCreateAt(now);
            }
            OwnerPersonalDTO personalDTO = dto.getOwnerPersonal();
            if (personalDTO == null) {
                throw new IllegalArgumentException("个人业主信息不能为空");
            }
            BeanUtils.copyProperties(personalDTO, personal);
            personal.setCompanyId(dto.getOwnerContract().getCompanyId());
            personal.setGender(personalDTO.getGender() == null ? null : personalDTO.getGender().getCode());
            personal.setIdType(personalDTO.getIdType() == null ? null : personalDTO.getIdType().getCode());
            personal.setPayeeIdType(personalDTO.getPayeeIdType() == null ? null : personalDTO.getPayeeIdType().getCode());
            personal.setTags(JSONUtil.toJsonStr(personalDTO.getTags()));
            personal.setStatus(Objects.requireNonNullElse(personalDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            personal.setUpdateBy(dto.getUpdateBy());
            personal.setUpdateAt(now);
            if (personal.getId() == null) {
                ownerPersonalRepo.save(personal);
            } else {
                ownerPersonalRepo.updateById(personal);
            }
            syncOwnerPersonalFiles(personal.getId(), personalDTO);
            owner.setOwnerType(OwnerTypeEnum.PERSONAL.getCode());
            owner.setOwnerTypeId(personal.getId());
            owner.setOwnerName(personal.getName());
            owner.setOwnerPhone(personal.getPhone());
        } else {
            OwnerCompany company;
            if (OwnerTypeEnum.COMPANY.equals(OwnerTypeEnum.fromCode(owner.getOwnerType())) && owner.getOwnerTypeId() != null) {
                company = ownerCompanyRepo.getById(owner.getOwnerTypeId());
                if (company == null) {
                    company = new OwnerCompany();
                }
            } else {
                company = new OwnerCompany();
                company.setCompanyId(dto.getOwnerContract().getCompanyId());
                company.setCreateBy(dto.getUpdateBy());
                company.setCreateAt(now);
            }
            OwnerCompanyDTO companyDTO = dto.getOwnerCompany();
            if (companyDTO == null) {
                throw new IllegalArgumentException("企业业主信息不能为空");
            }
            BeanUtils.copyProperties(companyDTO, company);
            company.setCompanyId(dto.getOwnerContract().getCompanyId());
            company.setLegalPersonIdType(companyDTO.getLegalPersonIdType() == null ? null : companyDTO.getLegalPersonIdType().getCode());
            company.setPayeeIdType(companyDTO.getPayeeIdType() == null ? null : companyDTO.getPayeeIdType().getCode());
            company.setTags(JSONUtil.toJsonStr(companyDTO.getTags()));
            company.setStatus(Objects.requireNonNullElse(companyDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            company.setUpdateBy(dto.getUpdateBy());
            company.setUpdateAt(now);
            if (company.getId() == null) {
                ownerCompanyRepo.save(company);
            } else {
                ownerCompanyRepo.updateById(company);
            }
            syncOwnerCompanyFiles(company.getId(), companyDTO);
            owner.setOwnerType(OwnerTypeEnum.COMPANY.getCode());
            owner.setOwnerTypeId(company.getId());
            owner.setOwnerName(company.getName());
            owner.setOwnerPhone(company.getContactPhone());
        }
        owner.setUpdateBy(dto.getUpdateBy());
        owner.setUpdateAt(now);
        ownerRepo.updateById(owner);
    }

    private void clearContractRelations(Long contractId) {
        ownerSettlementRuleRepo.deleteByContractIdForce(contractId);
        ownerSettlementFeeRepo.deleteByContractIdForce(contractId);
        ownerRentFreeRuleRepo.deleteByContractIdForce(contractId);
        ownerLeaseRuleRepo.deleteByContractIdForce(contractId);
        ownerLeaseFeeRepo.deleteByContractIdForce(contractId);
        ownerLeaseFreeRuleRepo.deleteByContractIdForce(contractId);
        ownerContractSubjectRepo.deleteByContractIdForce(contractId);
    }

    private void initOwnerAccount(Long companyId, Long ownerId, Date now) {
        OwnerAccount existing = ownerAccountRepo.getByOwnerId(ownerId);
        if (existing != null) {
            return;
        }
        OwnerAccount account = new OwnerAccount();
        account.setCompanyId(companyId);
        account.setOwnerId(ownerId);
        account.setAccountStatus(1);
        account.setAvailableAmount(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setPendingSettlementAmount(BigDecimal.ZERO);
        account.setTotalIncomeAmount(BigDecimal.ZERO);
        account.setTotalReductionAmount(BigDecimal.ZERO);
        account.setTotalWithdrawAmount(BigDecimal.ZERO);
        account.setVersion(0L);
        account.setCreateAt(now);
        account.setUpdateAt(now);
        ownerAccountRepo.save(account);
    }

    private String buildContractContent(OwnerContract contract, Long ownerId, List<OwnerContractSubjectDTO> subjectDTOs) {
        ContractTemplate template = contractTemplateRepo.getById(contract.getContractTemplateId());
        if (template == null || template.getTemplateContent() == null) {
            return contract.getContractContent();
        }
        Owner owner = ownerRepo.getById(ownerId);
        List<OwnerContractSubjectDTO> houseSubjects = Objects.requireNonNullElse(subjectDTOs, List.<OwnerContractSubjectDTO>of())
            .stream()
            .filter(item -> OwnerContractSubjectTypeEnum.HOUSE.equals(Objects.requireNonNullElse(item.getSubjectType(), OwnerContractSubjectTypeEnum.HOUSE)))
            .toList();
        List<House> houses = houseSubjects.stream()
            .map(item -> houseRepo.getById(item.getSubjectId()))
            .filter(Objects::nonNull)
            .toList();
        String subjectNames = Objects.requireNonNullElse(subjectDTOs, List.<OwnerContractSubjectDTO>of())
            .stream()
            .map(OwnerContractSubjectDTO::getSubjectName)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("，"));
        String content = template.getTemplateContent();
        content = content.replace(OwnerParamsEnum.CONTRACT_NUMBER.getKey(), contract.getContractNo());
        content = content.replace(OwnerParamsEnum.HOUSE_ADDRESS.getKey(), houses.stream().map(this::formatHouseAddress).collect(Collectors.joining("；")));
        content = content.replace(OwnerParamsEnum.PROJECT_NAME.getKey(), subjectNames);
        content = content.replace(OwnerParamsEnum.BUILDING_NUMBER.getKey(), houses.stream().map(House::getBuilding).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.UNIT_NUMBER.getKey(), houses.stream().map(House::getUnit).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.HOUSE_NUMBER.getKey(), houses.stream().map(House::getDoorNumber).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.SHARED_ROOM_NUMBER.getKey(), "");
        content = content.replace(OwnerParamsEnum.SIGNED_HOUSE_LIST.getKey(), subjectNames);
        content = content.replace(OwnerParamsEnum.HOUSE_PROPERTY_NUMBER.getKey(), houses.stream().map(House::getCertificateNo).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.HOUSE_TYPE.getKey(), "");
        content = content.replace(OwnerParamsEnum.PROPERTY_TYPE.getKey(), "");
        content = content.replace(OwnerParamsEnum.TOTAL_AREA.getKey(), houses.stream().map(House::getArea).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).toPlainString());
        content = content.replace(OwnerParamsEnum.SIGNED_AREA.getKey(), houses.stream().map(House::getArea).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).toPlainString());
        content = content.replace(OwnerParamsEnum.TENANT_NAME.getKey(), owner != null ? defaultString(owner.getOwnerName()) : "");
        return content;
    }

    private String generateContractNo() {
        return "OWN" + IdUtil.getSnowflakeNextIdStr();
    }

    private String formatHouseAddress(House house) {
        return defaultString(house.getBuilding()) + defaultString(house.getUnit()) + defaultString(house.getDoorNumber());
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private boolean hasMasterLeaseBillChange(OwnerContract currentContract, OwnerUpdateDTO dto) {
        OwnerCooperationModeEnum nextMode = dto.getOwnerContract().getCooperationMode();
        if (!OwnerCooperationModeEnum.MASTER_LEASE.equals(nextMode)) {
            return true;
        }

        OwnerDetailVO currentDetail = ownerContractQueryService.getOwnerContractDetail(buildOwnerContractIdDTO(currentContract.getId()));
        Map<String, Object> currentSnapshot = new LinkedHashMap<>();
        currentSnapshot.put("cooperationMode", currentContract.getCooperationMode());
        currentSnapshot.put("contractStart", formatDate(currentContract.getContractStart()));
        currentSnapshot.put("contractEnd", formatDate(currentContract.getContractEnd()));
        currentSnapshot.put("subjectList", normalizeContractSubjectList(currentDetail.getContractSubjectList()));
        currentSnapshot.put("leaseRule", normalizeOwnerLeaseRule(currentDetail.getOwnerLeaseRule()));
        currentSnapshot.put("leaseFreeRuleList", normalizeLeaseFreeRuleList(currentDetail.getOwnerLeaseFreeRuleList()));

        Map<String, Object> nextSnapshot = new LinkedHashMap<>();
        nextSnapshot.put("cooperationMode", nextMode.name());
        nextSnapshot.put("contractStart", formatDate(dto.getOwnerContract().getContractStart()));
        nextSnapshot.put("contractEnd", formatDate(dto.getOwnerContract().getContractEnd()));
        nextSnapshot.put("subjectList", normalizeContractSubjectList(dto.getContractSubjectList()));
        nextSnapshot.put("leaseRule", normalizeOwnerLeaseRule(dto.getOwnerLeaseRule()));
        nextSnapshot.put("leaseFreeRuleList", normalizeLeaseFreeRuleList(dto.getOwnerLeaseFreeRuleList()));
        return !Objects.equals(JSONUtil.toJsonStr(currentSnapshot), JSONUtil.toJsonStr(nextSnapshot));
    }

    private OwnerContractIdDTO buildOwnerContractIdDTO(Long contractId) {
        OwnerContractIdDTO dto = new OwnerContractIdDTO();
        dto.setContractId(contractId);
        return dto;
    }

    private List<Map<String, Object>> normalizeContractSubjectList(List<OwnerContractSubjectDTO> list) {
        return Objects.requireNonNullElse(list, List.<OwnerContractSubjectDTO>of())
            .stream()
            .map(item -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("subjectType", item.getSubjectType() == null ? null : item.getSubjectType().name());
                map.put("subjectId", item.getSubjectId());
                return map;
            })
            .sorted(Comparator.comparing(item -> String.valueOf(item.get("subjectType")) + "_" + String.valueOf(item.get("subjectId"))))
            .toList();
    }

    private Map<String, Object> normalizeOwnerLeaseRule(OwnerLeaseRuleDTO rule) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (rule == null) {
            return map;
        }
        map.put("rentAmount", rule.getRentAmount());
        map.put("depositAmount", rule.getDepositAmount());
        map.put("depositMonths", rule.getDepositMonths());
        map.put("paymentMonths", rule.getPaymentMonths());
        map.put("rentDueType", rule.getRentDueType() == null ? null : rule.getRentDueType().name());
        map.put("rentDueDay", rule.getRentDueDay());
        map.put("rentDueOffsetDays", rule.getRentDueOffsetDays());
        map.put("firstPayDate", formatDate(rule.getFirstPayDate()));
        map.put("billingStart", formatDate(rule.getBillingStart()));
        map.put("billingEnd", formatDate(rule.getBillingEnd()));
        map.put("prorateType", rule.getProrateType() == null ? null : rule.getProrateType().name());
        map.put("otherFeeList", normalizeLeaseFeeList(rule.getOtherFeeList()));
        return map;
    }

    private List<Map<String, Object>> normalizeLeaseFeeList(List<OwnerLeaseFeeDTO> list) {
        return Objects.requireNonNullElse(list, List.<OwnerLeaseFeeDTO>of())
            .stream()
            .map(item -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("dictDataId", item.getDictDataId());
                map.put("feeType", item.getFeeType());
                map.put("feeName", item.getFeeName());
                map.put("feeDirection", item.getFeeDirection() == null ? null : item.getFeeDirection().name());
                map.put("paymentMethod", item.getPaymentMethod());
                map.put("priceMethod", item.getPriceMethod());
                map.put("priceInput", item.getPriceInput());
                map.put("sortOrder", item.getSortOrder());
                map.put("remark", defaultString(item.getRemark()));
                return map;
            })
            .sorted(Comparator.comparing(item -> JSONUtil.toJsonStr(item)))
            .toList();
    }

    private List<Map<String, Object>> normalizeLeaseFreeRuleList(List<OwnerLeaseFreeRuleDTO> list) {
        return Objects.requireNonNullElse(list, List.<OwnerLeaseFreeRuleDTO>of())
            .stream()
            .map(item -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("freeType", item.getFreeType() == null ? null : item.getFreeType().name());
                map.put("startDate", formatDate(item.getStartDate()));
                map.put("endDate", formatDate(item.getEndDate()));
                map.put("calcMode", item.getCalcMode() == null ? null : item.getCalcMode().name());
                map.put("freeAmount", item.getFreeAmount());
                map.put("freeRatio", item.getFreeRatio());
                map.put("remark", defaultString(item.getRemark()));
                return map;
            })
            .sorted(Comparator.comparing(item -> JSONUtil.toJsonStr(item)))
            .toList();
    }

    private String formatDate(Date value) {
        return value == null ? null : DateUtil.formatDate(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private OwnerCreateDTO toCreateDTO(OwnerUpdateDTO dto) {
        OwnerCreateDTO createDTO = new OwnerCreateDTO();
        createDTO.setOwnerType(dto.getOwnerType());
        createDTO.setOwnerPersonal(dto.getOwnerPersonal());
        createDTO.setOwnerCompany(dto.getOwnerCompany());
        createDTO.setOwnerContract(dto.getOwnerContract());
        createDTO.setContractSubjectList(dto.getContractSubjectList());
        createDTO.setOwnerLeaseRule(dto.getOwnerLeaseRule());
        createDTO.setOwnerLeaseFreeRuleList(dto.getOwnerLeaseFreeRuleList());
        createDTO.setCreateBy(dto.getUpdateBy());
        return createDTO;
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private void saveSettlementItems(OwnerCreateDTO dto, OwnerContract contract, OwnerContractSubject subject, List<OwnerSettlementFeeDTO> items, Date now) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<OwnerSettlementFee> records = items.stream().map(item -> {
            OwnerSettlementFee record = new OwnerSettlementFee();
            record.setCompanyId(contract.getCompanyId());
            record.setContractId(contract.getId());
            record.setContractSubjectId(subject.getId());
            record.setFeeDirection(item.getFeeDirection());
            record.setFeeType(item.getFeeType());
            record.setDictDataId(item.getDictDataId());
            record.setFeeName(item.getFeeName());
            record.setTransferEnabled(Objects.requireNonNullElse(item.getTransferEnabled(), Boolean.FALSE));
            record.setTransferRatio(item.getTransferRatio());
            record.setSortOrder(Objects.requireNonNullElse(item.getSortOrder(), 0));
            record.setRemark(item.getRemark());
            record.setStatus(StatusEnum.ACTIVE.getValue());
            record.setCreateBy(dto.getCreateBy());
            record.setCreateAt(now);
            record.setUpdateBy(dto.getCreateBy());
            record.setUpdateAt(now);
            return record;
        }).toList();
        ownerSettlementFeeRepo.saveBatch(records);
    }

    private String resolveSubjectName(OwnerContractSubjectTypeEnum subjectType, Long subjectId, String fallbackName) {
        if (subjectType == null || subjectId == null) {
            return fallbackName;
        }
        if (OwnerContractSubjectTypeEnum.HOUSE.equals(subjectType)) {
            House house = houseRepo.getById(subjectId);
            if (house == null) {
                throw new IllegalArgumentException("房源不存在: " + subjectId);
            }
            return house.getHouseName();
        }
        if (OwnerContractSubjectTypeEnum.FOCUS.equals(subjectType)) {
            Focus focus = focusRepo.getById(subjectId);
            if (focus == null) {
                throw new IllegalArgumentException("集中式项目不存在: " + subjectId);
            }
            return focus.getFocusName();
        }
        if (OwnerContractSubjectTypeEnum.FOCUS_BUILDING.equals(subjectType)) {
            FocusBuilding focusBuilding = focusBuildingRepo.getById(subjectId);
            if (focusBuilding == null) {
                throw new IllegalArgumentException("集中式楼栋不存在: " + subjectId);
            }
            return buildFocusBuildingName(focusBuilding);
        }
        return fallbackName;
    }

    private String buildFocusBuildingName(FocusBuilding focusBuilding) {
        Focus focus = focusRepo.getById(focusBuilding.getFocusId());
        String focusName = focus == null ? "" : defaultString(focus.getFocusName());
        String building = defaultString(focusBuilding.getBuilding());
        String unit = defaultString(focusBuilding.getUnit());
        String suffix = building + unit;
        return isBlank(focusName) ? suffix : focusName + suffix;
    }

    private void saveLeaseFees(OwnerCreateDTO dto, Long contractId, Date now) {
        if (dto.getOwnerLeaseRule() == null || dto.getOwnerLeaseRule().getOtherFeeList() == null || dto.getOwnerLeaseRule().getOtherFeeList().isEmpty()) {
            return;
        }
        List<OwnerLeaseFee> records = dto.getOwnerLeaseRule().getOtherFeeList().stream().map(item -> {
            OwnerLeaseFee fee = new OwnerLeaseFee();
            fee.setCompanyId(dto.getOwnerContract().getCompanyId());
            fee.setContractId(contractId);
            fee.setDictDataId(item.getDictDataId());
            fee.setFeeType(item.getFeeType());
            fee.setFeeName(item.getFeeName());
            fee.setFeeDirection(enumName(item.getFeeDirection()));
            fee.setPaymentMethod(item.getPaymentMethod());
            fee.setPriceMethod(item.getPriceMethod());
            fee.setPriceInput(item.getPriceInput());
            fee.setSortOrder(Objects.requireNonNullElse(item.getSortOrder(), 0));
            fee.setRemark(item.getRemark());
            fee.setStatus(StatusEnum.ACTIVE.getValue());
            fee.setCreateBy(dto.getCreateBy());
            fee.setCreateAt(now);
            fee.setUpdateBy(dto.getCreateBy());
            fee.setUpdateAt(now);
            return fee;
        }).toList();
        ownerLeaseFeeRepo.saveBatch(records);
    }

    private void syncOwnerPersonalFiles(Long bizId, OwnerPersonalDTO dto) {
        if (bizId == null || dto == null) return;
        fileAttachRepo.recreateFileAttachList(bizId, FileAttachBizTypeEnum.OWNER_ID_CARD_FRONT.getBizType(), CollUtil.emptyIfNull(dto.getIdCardFrontList()));
        fileAttachRepo.recreateFileAttachList(bizId, FileAttachBizTypeEnum.OWNER_ID_CARD_BACK.getBizType(), CollUtil.emptyIfNull(dto.getIdCardBackList()));
        fileAttachRepo.recreateFileAttachList(bizId, FileAttachBizTypeEnum.OWNER_ID_CARD_IN_HAND.getBizType(), CollUtil.emptyIfNull(dto.getIdCardInHandList()));
        fileAttachRepo.recreateFileAttachList(bizId, FileAttachBizTypeEnum.OWNER_OTHER_IMAGE.getBizType(), CollUtil.emptyIfNull(dto.getOtherImageList()));
    }

    private void syncOwnerCompanyFiles(Long bizId, OwnerCompanyDTO dto) {
        if (bizId == null || dto == null) return;
        fileAttachRepo.recreateFileAttachList(bizId, FileAttachBizTypeEnum.OWNER_BUSINESS_LICENSE.getBizType(), CollUtil.emptyIfNull(dto.getBusinessLicenseUrls()));
    }
}
