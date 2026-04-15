package com.homi.service.service.owner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.contract.OwnerParamsEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.owner.*;
import com.homi.common.lib.enums.price.PaymentMethodEnum;
import com.homi.common.lib.enums.price.PriceMethodEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.owner.dto.*;
import com.homi.model.owner.vo.OwnerContractTotalVO;
import com.homi.model.owner.vo.OwnerDetailVO;
import com.homi.model.owner.vo.OwnerListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerContractService {
    private final OwnerRepo ownerRepo;
    private final OwnerPersonalRepo ownerPersonalRepo;
    private final OwnerCompanyRepo ownerCompanyRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractSubjectRepo ownerContractSubjectRepo;
    private final OwnerSettlementRuleRepo ownerSettlementRuleRepo;
    private final OwnerSettlementItemRepo ownerSettlementItemRepo;
    private final OwnerRentFreeRuleRepo ownerRentFreeRuleRepo;
    private final OwnerLeaseRuleRepo ownerLeaseRuleRepo;
    private final OwnerLeaseFeeRepo ownerLeaseFeeRepo;
    private final OwnerLeaseFreeRuleRepo ownerLeaseFreeRuleRepo;
    private final OwnerAccountRepo ownerAccountRepo;
    private final ContractTemplateRepo contractTemplateRepo;
    private final HouseRepo houseRepo;
    private final FocusRepo focusRepo;
    private final FocusBuildingRepo focusBuildingRepo;
    private final UserRepo userRepo;
    private final FileAttachRepo fileAttachRepo;
    private final OwnerBillingGenerateService ownerBillingGenerateService;

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
        contract.setCreateBy(dto.getCreateBy());
        contract.setCreateTime(now);
        contract.setUpdateBy(dto.getCreateBy());
        contract.setUpdateTime(now);
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

    public PageVO<OwnerListVO> getOwnerContractList(OwnerQueryDTO query) {
        Page<OwnerContract> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        LambdaQueryWrapper<OwnerContract> wrapper = buildOwnerContractWrapper(query, false);
        List<Long> ownerIds = resolveOwnerIds(query);
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyPage(query);
        }
        wrapper.orderByDesc(OwnerContract::getCreateTime);
        Page<OwnerContract> result = ownerContractRepo.page(page, wrapper);

        List<OwnerListVO> list = result.getRecords().stream().map(contract -> toListVO(contract)).filter(Objects::nonNull).toList();
        return PageVO.<OwnerListVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(result.getTotal())
            .pages(result.getPages())
            .list(list)
            .build();
    }

    public OwnerContractTotalVO getOwnerContractTotal(OwnerQueryDTO query) {
        OwnerContractTotalVO vo = new OwnerContractTotalVO();
        LambdaQueryWrapper<OwnerContract> wrapper = buildOwnerContractWrapper(query, true);
        List<OwnerContract> contracts = ownerContractRepo.list(wrapper);
        DateTime now = DateUtil.date();
        DateTime expireLimit = DateUtil.offsetDay(now, 30);

        vo.setTotal(contracts.size());
        vo.setActiveTotal((int) contracts.stream().filter(item -> Objects.equals(item.getStatus(), StatusEnum.ACTIVE.getValue())).count());
        vo.setDisabledTotal((int) contracts.stream().filter(item -> Objects.equals(item.getStatus(), StatusEnum.DISABLED.getValue())).count());
        vo.setPendingSignTotal((int) contracts.stream().filter(item -> Objects.equals(item.getSignStatus(), OwnerSignStatusEnum.PENDING.getCode())).count());
        vo.setSignedTotal((int) contracts.stream().filter(item -> Objects.equals(item.getSignStatus(), OwnerSignStatusEnum.SIGNED.getCode())).count());
        vo.setExpiring30DaysTotal((int) contracts.stream()
            .filter(item -> item.getContractEnd() != null)
            .filter(item -> !item.getContractEnd().before(now) && !item.getContractEnd().after(expireLimit))
            .count());
        return vo;
    }

    public OwnerDetailVO getOwnerContractDetail(OwnerContractIdDTO dto) {
        if (dto == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("合同ID不能为空");
        }
        OwnerContract contract = ownerContractRepo.getById(dto.getContractId());
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        Owner owner = ownerRepo.getById(contract.getOwnerId());
        if (owner == null) {
            throw new IllegalArgumentException("业主不存在");
        }

        OwnerDetailVO vo = new OwnerDetailVO();
        vo.setOwnerId(owner.getId());
        vo.setOwnerType(ownerTypeOf(owner.getOwnerType()));
        vo.setOwnerContract(toOwnerContractDTO(contract));
        ContractTemplate template = contractTemplateRepo.getById(contract.getContractTemplateId());
        if (template != null) {
            vo.setContractTemplateName(template.getTemplateName());
        }
        if (OwnerTypeEnum.PERSONAL.equals(vo.getOwnerType())) {
            OwnerPersonal personal = ownerPersonalRepo.getById(owner.getOwnerTypeId());
            if (personal != null) {
                vo.setOwnerPersonal(toOwnerPersonalDTO(personal));
            }
        } else {
            OwnerCompany company = ownerCompanyRepo.getById(owner.getOwnerTypeId());
            if (company != null) {
                vo.setOwnerCompany(toOwnerCompanyDTO(company));
            }
        }

        List<OwnerContractSubject> contractSubjects = ownerContractSubjectRepo.listByContractId(contract.getId());
        List<OwnerContractSubjectDTO> subjectDTOList = contractSubjects.stream().map(item -> {
            OwnerContractSubjectDTO subjectDTO = new OwnerContractSubjectDTO();
            subjectDTO.setId(item.getId());
            subjectDTO.setSubjectType(OwnerContractSubjectTypeEnum.fromCode(item.getSubjectType()));
            subjectDTO.setSubjectId(item.getSubjectId());
            subjectDTO.setSubjectName(item.getSubjectNameSnapshot());
            subjectDTO.setRemark(item.getRemark());
            return subjectDTO;
        }).collect(Collectors.toList());

        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            List<OwnerSettlementRule> settlementRules = ownerSettlementRuleRepo.list(
                new LambdaQueryWrapper<OwnerSettlementRule>().eq(OwnerSettlementRule::getContractId, contract.getId())
            );
            List<OwnerRentFreeRule> rentFreeRules = ownerRentFreeRuleRepo.list(
                new LambdaQueryWrapper<OwnerRentFreeRule>().eq(OwnerRentFreeRule::getContractId, contract.getId())
            );
            for (OwnerContractSubjectDTO subjectDTO : subjectDTOList) {
                OwnerSettlementRule settlementRule = settlementRules.stream()
                    .filter(item -> Objects.equals(item.getContractSubjectId(), subjectDTO.getId()))
                    .findFirst()
                    .orElse(null);
                if (settlementRule != null) {
                    subjectDTO.setSettlementRule(toOwnerSettlementRuleDTO(settlementRule));
                }
                OwnerRentFreeRule rentFreeRule = rentFreeRules.stream()
                    .filter(item -> Objects.equals(item.getContractSubjectId(), subjectDTO.getId()))
                    .findFirst()
                    .orElse(null);
                if (rentFreeRule != null) {
                    subjectDTO.setRentFreeRule(toOwnerRentFreeRuleDTO(rentFreeRule));
                }
            }
        } else {
            OwnerLeaseRule leaseRule = ownerLeaseRuleRepo.getOne(
                new LambdaQueryWrapper<OwnerLeaseRule>().eq(OwnerLeaseRule::getContractId, contract.getId()).last("limit 1")
            );
            if (leaseRule != null) {
                vo.setOwnerLeaseRule(toOwnerLeaseRuleDTO(leaseRule));
            }
            List<OwnerLeaseFreeRuleDTO> leaseFreeRuleList = ownerLeaseFreeRuleRepo.list(
                new LambdaQueryWrapper<OwnerLeaseFreeRule>().eq(OwnerLeaseFreeRule::getContractId, contract.getId())
            ).stream().map(this::toOwnerLeaseFreeRuleDTO).collect(Collectors.toList());
            vo.setOwnerLeaseFreeRuleList(leaseFreeRuleList);
            boolean masterLeaseBillLocked = ownerBillingGenerateService.isMasterLeaseBillLocked(contract.getId());
            vo.setMasterLeaseBillLocked(masterLeaseBillLocked);
            vo.setMasterLeaseBillLockReason(masterLeaseBillLocked ? "该包租合同已发生付款或结算，账单条款已锁定；如需调整，请走合同变更。" : null);
        }
        vo.setContractSubjectList(subjectDTOList);
        ContractSubjectSummary summary = buildContractSubjectSummary(contract, contractSubjects);
        vo.setSubjectCount(summary.subjectCount());
        vo.setTotalArea(summary.totalArea());
        vo.setConfiguredSubjectCount(summary.configuredSubjectCount());
        vo.setCreateBy(contract.getCreateBy());
        vo.setCreateTime(contract.getCreateTime());
        vo.setUpdateBy(contract.getUpdateBy());
        vo.setUpdateTime(contract.getUpdateTime());
        Map<Long, String> userNameMap = getUserNameMap(contract.getCreateBy(), contract.getUpdateBy());
        vo.setCreateByName(userNameMap.get(contract.getCreateBy()));
        vo.setUpdateByName(userNameMap.get(contract.getUpdateBy()));
        return vo;
    }

    public byte[] previewOwnerContract(OwnerContractIdDTO dto) {
        if (dto == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("合同ID不能为空");
        }
        OwnerContract contract = ownerContractRepo.getById(dto.getContractId());
        if (contract == null || contract.getContractContent() == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        return ConvertHtml2PdfUtils.generatePdf(contract.getContractContent());
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
        contract.setUpdateBy(dto.getUpdateBy());
        contract.setUpdateTime(now);
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

    @Transactional(rollbackFor = Exception.class)
    public Long updateOwnerContractStatus(OwnerContractStatusDTO dto, Long updateBy) {
        if (dto == null || dto.getContractId() == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("合同状态参数不能为空");
        }
        OwnerContract contract = ownerContractRepo.getById(dto.getContractId());
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        Date now = DateUtil.date();
        contract.setStatus(dto.getStatus().getValue());
        contract.setUpdateBy(updateBy);
        contract.setUpdateTime(now);
        ownerContractRepo.updateById(contract);

        ownerContractSubjectRepo.listByContractId(contract.getId()).forEach(item -> {
            item.setStatus(dto.getStatus().getValue());
            item.setUpdateBy(updateBy);
            item.setUpdateTime(now);
            ownerContractSubjectRepo.updateById(item);
        });
        return contract.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long deleteOwnerContract(OwnerContractIdDTO dto, Long updateBy) {
        if (dto == null || dto.getContractId() == null) {
            throw new IllegalArgumentException("合同ID不能为空");
        }
        OwnerContract contract = ownerContractRepo.getById(dto.getContractId());
        if (contract == null) {
            throw new IllegalArgumentException("业主合同不存在");
        }
        if (OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            ownerBillingGenerateService.clearMasterLeasePayableBillsByContract(contract.getId());
        }
        contract.setUpdateBy(updateBy);
        contract.setUpdateTime(DateUtil.date());
        ownerContractRepo.updateById(contract);
        clearContractRelations(contract.getId());
        ownerContractRepo.removeById(contract.getId());
        return contract.getId();
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
            record.setCreateTime(now);
            record.setUpdateBy(dto.getCreateBy());
            record.setUpdateTime(now);
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
                rule.setCreateTime(now);
                rule.setUpdateBy(dto.getCreateBy());
                rule.setUpdateTime(now);
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
                rule.setCreateTime(now);
                rule.setUpdateBy(dto.getCreateBy());
                rule.setUpdateTime(now);
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
        leaseRule.setCreateTime(now);
        leaseRule.setUpdateBy(dto.getCreateBy());
        leaseRule.setUpdateTime(now);
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
            rule.setCreateTime(now);
            rule.setUpdateBy(dto.getCreateBy());
            rule.setUpdateTime(now);
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
            personal.setCreateTime(now);
            personal.setUpdateBy(dto.getCreateBy());
            personal.setUpdateTime(now);
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
            company.setCreateTime(now);
            company.setUpdateBy(dto.getCreateBy());
            company.setUpdateTime(now);
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
        owner.setCreateTime(now);
        owner.setUpdateBy(dto.getCreateBy());
        owner.setUpdateTime(now);
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
            if (OwnerTypeEnum.PERSONAL.equals(ownerTypeOf(owner.getOwnerType())) && owner.getOwnerTypeId() != null) {
                personal = ownerPersonalRepo.getById(owner.getOwnerTypeId());
                if (personal == null) {
                    personal = new OwnerPersonal();
                }
            } else {
                personal = new OwnerPersonal();
                personal.setCompanyId(dto.getOwnerContract().getCompanyId());
                personal.setCreateBy(dto.getUpdateBy());
                personal.setCreateTime(now);
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
            personal.setUpdateTime(now);
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
            if (OwnerTypeEnum.COMPANY.equals(ownerTypeOf(owner.getOwnerType())) && owner.getOwnerTypeId() != null) {
                company = ownerCompanyRepo.getById(owner.getOwnerTypeId());
                if (company == null) {
                    company = new OwnerCompany();
                }
            } else {
                company = new OwnerCompany();
                company.setCompanyId(dto.getOwnerContract().getCompanyId());
                company.setCreateBy(dto.getUpdateBy());
                company.setCreateTime(now);
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
            company.setUpdateTime(now);
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
        owner.setUpdateTime(now);
        ownerRepo.updateById(owner);
    }

    private void clearContractRelations(Long contractId) {
        ownerSettlementRuleRepo.deleteByContractIdForce(contractId);
        ownerSettlementItemRepo.deleteByContractIdForce(contractId);
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
        account.setCreateTime(now);
        account.setUpdateTime(now);
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

    private OwnerListVO toListVO(OwnerContract contract) {
        Owner owner = ownerRepo.getById(contract.getOwnerId());
        if (owner == null) {
            return null;
        }
        OwnerListVO vo = new OwnerListVO();
        vo.setContractId(contract.getId());
        vo.setOwnerId(owner.getId());
        vo.setOwnerType(ownerTypeOf(owner.getOwnerType()));
        vo.setOwnerName(owner.getOwnerName());
        vo.setOwnerPhone(owner.getOwnerPhone());
        vo.setContractNo(contract.getContractNo());
        vo.setContractStart(contract.getContractStart());
        vo.setContractEnd(contract.getContractEnd());
        vo.setCooperationMode(contract.getCooperationMode() == null ? null : OwnerCooperationModeEnum.valueOf(contract.getCooperationMode()));
        vo.setSignStatus(signStatusOf(contract.getSignStatus()));
        vo.setStatus(statusOf(contract.getStatus()));
        vo.setCreateTime(contract.getCreateTime());
        vo.setUpdateTime(contract.getUpdateTime());

        ContractTemplate template = contractTemplateRepo.getById(contract.getContractTemplateId());
        if (template != null) {
            vo.setContractTemplateName(template.getTemplateName());
        }
        List<OwnerContractSubject> contractSubjects = ownerContractSubjectRepo.listByContractId(contract.getId());
        vo.setSubjectNames(contractSubjects.stream().map(OwnerContractSubject::getSubjectNameSnapshot).collect(Collectors.joining("，")));
        ContractSubjectSummary summary = buildContractSubjectSummary(contract, contractSubjects);
        vo.setSubjectCount(summary.subjectCount());
        vo.setTotalArea(summary.totalArea());
        vo.setConfiguredSubjectCount(summary.configuredSubjectCount());
        vo.setOwnerTag(resolveOwnerTag(owner));
        return vo;
    }

    private LambdaQueryWrapper<OwnerContract> buildOwnerContractWrapper(OwnerQueryDTO query, boolean ignoreStatusFilters) {
        LambdaQueryWrapper<OwnerContract> wrapper = new LambdaQueryWrapper<>();
        List<Long> ownerIds = resolveOwnerIds(query);
        if (ownerIds != null && ownerIds.isEmpty()) {
            wrapper.eq(OwnerContract::getId, -1L);
            return wrapper;
        }
        wrapper.in(ownerIds != null, OwnerContract::getOwnerId, ownerIds);
        wrapper.eq(query.getCooperationMode() != null, OwnerContract::getCooperationMode, query.getCooperationMode() == null ? null : query.getCooperationMode().name());
        if (!ignoreStatusFilters) {
            wrapper.eq(Objects.nonNull(query.getStatus()), OwnerContract::getStatus, query.getStatus() == null ? null : query.getStatus().getValue());
            wrapper.eq(Objects.nonNull(query.getSignStatus()), OwnerContract::getSignStatus, query.getSignStatus() == null ? null : query.getSignStatus().getCode());
            if (query.getExpiringDaysWithin() != null) {
                wrapper.ge(OwnerContract::getContractEnd, DateUtil.beginOfDay(new Date()));
                wrapper.le(OwnerContract::getContractEnd, DateUtil.endOfDay(DateUtil.offsetDay(new Date(), query.getExpiringDaysWithin())));
            }
        }
        return wrapper;
    }

    private ContractSubjectSummary buildContractSubjectSummary(OwnerContract contract, List<OwnerContractSubject> contractSubjects) {
        List<Long> houseIds = contractSubjects.stream()
            .filter(item -> OwnerContractSubjectTypeEnum.HOUSE.getCode().equals(item.getSubjectType()))
            .map(OwnerContractSubject::getSubjectId)
            .filter(Objects::nonNull)
            .toList();
        List<House> houses = houseIds.isEmpty() ? List.of() : houseRepo.listByIds(houseIds);
        BigDecimal totalArea = houses.stream()
            .map(House::getArea)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int configuredHouseCount;
        if (OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            configuredHouseCount = ownerLeaseRuleRepo.count(new LambdaQueryWrapper<OwnerLeaseRule>().eq(OwnerLeaseRule::getContractId, contract.getId())) > 0 ? contractSubjects.size() : 0;
        } else {
            List<Long> configuredIds = ownerSettlementRuleRepo.list(new LambdaQueryWrapper<OwnerSettlementRule>().eq(OwnerSettlementRule::getContractId, contract.getId()))
                .stream()
                .map(OwnerSettlementRule::getContractSubjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            configuredHouseCount = configuredIds.size();
        }
        return new ContractSubjectSummary(contractSubjects.size(), totalArea, configuredHouseCount);
    }

    private Map<Long, String> getUserNameMap(Long... userIds) {
        List<Long> idList = Arrays.stream(userIds).filter(Objects::nonNull).distinct().toList();
        if (idList.isEmpty()) {
            return Map.of();
        }
        List<User> users = userRepo.listByIds(idList);
        Map<Long, String> result = new HashMap<>();
        for (User user : users) {
            result.put(user.getId(), user.getRealName() != null && !user.getRealName().isBlank() ? user.getRealName() : user.getNickname());
        }
        return result;
    }

    private record ContractSubjectSummary(Integer subjectCount, BigDecimal totalArea, Integer configuredSubjectCount) {
    }

    private List<Long> resolveOwnerIds(OwnerQueryDTO query) {
        if (query.getOwnerType() == null && isBlank(query.getOwnerName()) && isBlank(query.getOwnerPhone())) {
            return null;
        }
        LambdaQueryWrapper<Owner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getOwnerType() != null, Owner::getOwnerType, query.getOwnerType() == null ? null : query.getOwnerType().getCode());
        wrapper.like(!isBlank(query.getOwnerName()), Owner::getOwnerName, query.getOwnerName());
        wrapper.like(!isBlank(query.getOwnerPhone()), Owner::getOwnerPhone, query.getOwnerPhone());
        return ownerRepo.list(wrapper).stream().map(Owner::getId).toList();
    }

    private PageVO<OwnerListVO> emptyPage(OwnerQueryDTO query) {
        return PageVO.<OwnerListVO>builder()
            .currentPage(query.getCurrentPage())
            .pageSize(query.getPageSize())
            .total(0L)
            .pages(0L)
            .list(List.of())
            .build();
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

        OwnerDetailVO currentDetail = getOwnerContractDetail(buildOwnerContractIdDTO(currentContract.getId()));
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

    private OwnerTypeEnum ownerTypeOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (OwnerTypeEnum item : OwnerTypeEnum.values()) {
            if (Objects.equals(item.getCode(), code)) {
                return item;
            }
        }
        return null;
    }

    private OwnerSignStatusEnum signStatusOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (OwnerSignStatusEnum item : OwnerSignStatusEnum.values()) {
            if (Objects.equals(item.getCode(), code)) {
                return item;
            }
        }
        return null;
    }

    private StatusEnum statusOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum item : StatusEnum.values()) {
            if (item.getValue() == code) {
                return item;
            }
        }
        return null;
    }

    private OwnerContractDTO toOwnerContractDTO(OwnerContract contract) {
        OwnerContractDTO dto = new OwnerContractDTO();
        dto.setId(contract.getId());
        dto.setCompanyId(contract.getCompanyId());
        dto.setOwnerId(contract.getOwnerId());
        dto.setCooperationMode(contract.getCooperationMode() == null ? null : OwnerCooperationModeEnum.valueOf(contract.getCooperationMode()));
        dto.setContractNo(contract.getContractNo());
        dto.setContractTemplateId(contract.getContractTemplateId());
        dto.setContractContent(contract.getContractContent());
        dto.setSignStatus(signStatusOf(contract.getSignStatus()));
        dto.setSignType(contract.getSignType() == null ? null : OwnerSignTypeEnum.valueOf(contract.getSignType()));
        dto.setContractMedium(contract.getContractMedium() == null ? null : OwnerContractMediumEnum.valueOf(contract.getContractMedium()));
        dto.setNotifyOwner(Objects.requireNonNullElse(contract.getNotifyOwner(), Boolean.FALSE));
        dto.setContractStart(contract.getContractStart());
        dto.setContractEnd(contract.getContractEnd());
        dto.setStatus(statusOf(contract.getStatus()));
        dto.setApprovalStatus(BizApprovalStatusEnum.getByCode(contract.getApprovalStatus()));
        dto.setRemark(contract.getRemark());
        dto.setCreateBy(contract.getCreateBy());
        dto.setCreateTime(contract.getCreateTime());
        dto.setUpdateBy(contract.getUpdateBy());
        dto.setUpdateTime(contract.getUpdateTime());
        return dto;
    }

    private OwnerPersonalDTO toOwnerPersonalDTO(OwnerPersonal personal) {
        OwnerPersonalDTO dto = new OwnerPersonalDTO();
        dto.setId(personal.getId());
        dto.setCompanyId(personal.getCompanyId());
        dto.setName(personal.getName());
        dto.setGender(personal.getGender() == null ? null : genderOf(personal.getGender()));
        dto.setIdType(personal.getIdType() == null ? null : idTypeOf(personal.getIdType()));
        dto.setIdNo(personal.getIdNo());
        dto.setPhone(personal.getPhone());
        dto.setPayeeName(personal.getPayeeName());
        dto.setPayeePhone(personal.getPayeePhone());
        dto.setPayeeIdType(personal.getPayeeIdType() == null ? null : idTypeOf(personal.getPayeeIdType()));
        dto.setPayeeIdNo(personal.getPayeeIdNo());
        dto.setBankAccountName(personal.getBankAccountName());
        dto.setBankAccountNo(personal.getBankAccountNo());
        dto.setBankName(personal.getBankName());
        dto.setIdCardFrontList(getFileUrls(personal.getId(), FileAttachBizTypeEnum.OWNER_ID_CARD_FRONT.getBizType()));
        dto.setIdCardBackList(getFileUrls(personal.getId(), FileAttachBizTypeEnum.OWNER_ID_CARD_BACK.getBizType()));
        dto.setIdCardInHandList(getFileUrls(personal.getId(), FileAttachBizTypeEnum.OWNER_ID_CARD_IN_HAND.getBizType()));
        dto.setOtherImageList(getFileUrls(personal.getId(), FileAttachBizTypeEnum.OWNER_OTHER_IMAGE.getBizType()));
        dto.setTags(parseTags(personal.getTags()));
        dto.setRemark(personal.getRemark());
        dto.setStatus(statusOf(personal.getStatus()));
        dto.setCreateBy(personal.getCreateBy());
        return dto;
    }

    private OwnerCompanyDTO toOwnerCompanyDTO(OwnerCompany company) {
        OwnerCompanyDTO dto = new OwnerCompanyDTO();
        dto.setId(company.getId());
        dto.setCompanyId(company.getCompanyId());
        dto.setName(company.getName());
        dto.setUscc(company.getUscc());
        dto.setLegalPerson(company.getLegalPerson());
        dto.setLegalPersonIdType(company.getLegalPersonIdType() == null ? null : idTypeOf(company.getLegalPersonIdType()));
        dto.setLegalPersonIdNo(company.getLegalPersonIdNo());
        dto.setContactName(company.getContactName());
        dto.setContactPhone(company.getContactPhone());
        dto.setPayeeName(company.getPayeeName());
        dto.setPayeePhone(company.getPayeePhone());
        dto.setPayeeIdType(company.getPayeeIdType() == null ? null : idTypeOf(company.getPayeeIdType()));
        dto.setPayeeIdNo(company.getPayeeIdNo());
        dto.setBankAccountName(company.getBankAccountName());
        dto.setBankAccountNo(company.getBankAccountNo());
        dto.setBankName(company.getBankName());
        dto.setBusinessLicenseUrls(getFileUrls(company.getId(), FileAttachBizTypeEnum.OWNER_BUSINESS_LICENSE.getBizType()));
        dto.setRegisteredAddress(company.getRegisteredAddress());
        dto.setTags(parseTags(company.getTags()));
        dto.setRemark(company.getRemark());
        dto.setStatus(statusOf(company.getStatus()));
        dto.setCreateBy(company.getCreateBy());
        return dto;
    }

    private OwnerSettlementRuleDTO toOwnerSettlementRuleDTO(OwnerSettlementRule rule) {
        OwnerSettlementRuleDTO dto = new OwnerSettlementRuleDTO();
        dto.setIncomeBasis(rule.getIncomeBasis() == null ? null : com.homi.common.lib.enums.owner.OwnerIncomeBasisEnum.valueOf(rule.getIncomeBasis()));
        dto.setSettlementMode(rule.getSettlementMode() == null ? null : com.homi.common.lib.enums.owner.OwnerSettlementModeEnum.valueOf(rule.getSettlementMode()));
        dto.setGuaranteedRentAmount(rule.getGuaranteedRentAmount());
        dto.setHasGuaranteedRent(Objects.requireNonNullElse(rule.getHasGuaranteedRent(), Boolean.FALSE));
        dto.setCommissionMode(rule.getCommissionMode() == null ? null : com.homi.common.lib.enums.owner.OwnerFeeModeEnum.valueOf(rule.getCommissionMode()));
        dto.setCommissionValue(rule.getCommissionValue());
        dto.setServiceFeeMode(rule.getServiceFeeMode() == null ? null : com.homi.common.lib.enums.owner.OwnerFeeModeEnum.valueOf(rule.getServiceFeeMode()));
        dto.setServiceFeeValue(rule.getServiceFeeValue());
        dto.setManagementFeeEnabled(Objects.requireNonNullElse(rule.getManagementFeeEnabled(), Boolean.FALSE));
        dto.setManagementFeeMode(rule.getManagementFeeMode() == null ? null : com.homi.common.lib.enums.owner.OwnerFeeModeEnum.valueOf(rule.getManagementFeeMode()));
        dto.setManagementFeeValue(rule.getManagementFeeValue());
        dto.setBearTaxType(rule.getBearTaxType() == null ? null : com.homi.common.lib.enums.owner.OwnerBearTypeEnum.valueOf(rule.getBearTaxType()));
        dto.setPaymentFeeBearType(rule.getPaymentFeeBearType() == null ? null : OwnerPaymentFeeBearTypeEnum.valueOf(rule.getPaymentFeeBearType()));
        dto.setSettlementTiming(rule.getSettlementTiming() == null ? null : OwnerSettlementTimingEnum.valueOf(rule.getSettlementTiming()));
        dto.setRentFreeEnabled(Objects.requireNonNullElse(rule.getRentFreeEnabled(), Boolean.FALSE));
        dto.setSettlementItemList(ownerSettlementItemRepo.list(new LambdaQueryWrapper<OwnerSettlementItem>()
                .eq(OwnerSettlementItem::getContractId, rule.getContractId())
                .eq(OwnerSettlementItem::getContractSubjectId, rule.getContractSubjectId()))
            .stream()
            .map(this::toOwnerSettlementItemDTO)
            .toList());
        dto.setEffectiveStart(rule.getEffectiveStart());
        dto.setEffectiveEnd(rule.getEffectiveEnd());
        dto.setStatus(statusOf(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        return dto;
    }

    private OwnerRentFreeRuleDTO toOwnerRentFreeRuleDTO(OwnerRentFreeRule rule) {
        OwnerRentFreeRuleDTO dto = new OwnerRentFreeRuleDTO();
        dto.setEnabled(Objects.requireNonNullElse(rule.getEnabled(), Boolean.FALSE));
        dto.setFreeType(rule.getFreeType() == null ? null : com.homi.common.lib.enums.owner.OwnerFreeTypeEnum.valueOf(rule.getFreeType()));
        dto.setStartDate(rule.getStartDate());
        dto.setEndDate(rule.getEndDate());
        dto.setBearType(rule.getBearType() == null ? null : com.homi.common.lib.enums.owner.OwnerBearTypeEnum.valueOf(rule.getBearType()));
        dto.setOwnerRatio(rule.getOwnerRatio());
        dto.setPlatformRatio(rule.getPlatformRatio());
        dto.setCalcMode(rule.getCalcMode() == null ? null : com.homi.common.lib.enums.owner.OwnerFreeCalcModeEnum.valueOf(rule.getCalcMode()));
        dto.setStatus(statusOf(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        return dto;
    }

    private OwnerLeaseRuleDTO toOwnerLeaseRuleDTO(OwnerLeaseRule rule) {
        OwnerLeaseRuleDTO dto = new OwnerLeaseRuleDTO();
        dto.setRentAmount(rule.getRentAmount());
        dto.setDepositAmount(rule.getDepositAmount());
        dto.setDepositMonths(rule.getDepositMonths());
        dto.setPaymentMonths(rule.getPaymentMonths());
        dto.setPayWay(rule.getPayWay());
        dto.setRentDueType(rule.getRentDueType() == null ? null : com.homi.common.lib.enums.lease.LeaseRentDueTypeEnum.values()[rule.getRentDueType() - 1]);
        dto.setRentDueDay(rule.getRentDueDay());
        dto.setRentDueOffsetDays(rule.getRentDueOffsetDays());
        dto.setFirstPayDate(rule.getFirstPayDate());
        dto.setHandoverDate(rule.getHandoverDate());
        dto.setUsageType(rule.getUsageType());
        dto.setBillingStart(rule.getBillingStart());
        dto.setBillingEnd(rule.getBillingEnd());
        dto.setProrateType(rule.getProrateType() == null ? null : com.homi.common.lib.enums.owner.OwnerProrateTypeEnum.valueOf(rule.getProrateType()));
        dto.setStatus(statusOf(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        dto.setOtherFeeList(ownerLeaseFeeRepo.list(new LambdaQueryWrapper<OwnerLeaseFee>().eq(OwnerLeaseFee::getContractId, rule.getContractId()))
            .stream()
            .map(this::toOwnerLeaseFeeDTO)
            .toList());
        return dto;
    }

    private OwnerLeaseFreeRuleDTO toOwnerLeaseFreeRuleDTO(OwnerLeaseFreeRule rule) {
        OwnerLeaseFreeRuleDTO dto = new OwnerLeaseFreeRuleDTO();
        dto.setFreeType(rule.getFreeType() == null ? null : com.homi.common.lib.enums.owner.OwnerFreeTypeEnum.valueOf(rule.getFreeType()));
        dto.setStartDate(rule.getStartDate());
        dto.setEndDate(rule.getEndDate());
        dto.setCalcMode(rule.getCalcMode() == null ? null : com.homi.common.lib.enums.owner.OwnerFreeCalcModeEnum.valueOf(rule.getCalcMode()));
        dto.setFreeAmount(rule.getFreeAmount());
        dto.setFreeRatio(rule.getFreeRatio());
        dto.setStatus(statusOf(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        return dto;
    }

    private OwnerSettlementItemDTO toOwnerSettlementItemDTO(OwnerSettlementItem item) {
        OwnerSettlementItemDTO dto = new OwnerSettlementItemDTO();
        dto.setFeeDirection(item.getFeeDirection());
        dto.setFeeType(item.getFeeType());
        dto.setItemName(item.getItemName());
        dto.setTransferEnabled(Objects.requireNonNullElse(item.getTransferEnabled(), Boolean.FALSE));
        dto.setTransferRatio(item.getTransferRatio());
        dto.setSortOrder(item.getSortOrder());
        dto.setRemark(item.getRemark());
        return dto;
    }

    private OwnerLeaseFeeDTO toOwnerLeaseFeeDTO(OwnerLeaseFee fee) {
        OwnerLeaseFeeDTO dto = new OwnerLeaseFeeDTO();
        dto.setFeeType(fee.getFeeType());
        dto.setFeeName(fee.getFeeName());
        dto.setFeeDirection(fee.getFeeDirection() == null ? null : FinanceFlowDirectionEnum.valueOf(fee.getFeeDirection()));
        dto.setPaymentMethod(fee.getPaymentMethod());
        dto.setPriceMethod(fee.getPriceMethod());
        dto.setPriceInput(fee.getPriceInput());
        dto.setSortOrder(fee.getSortOrder());
        dto.setRemark(fee.getRemark());
        return dto;
    }

    private com.homi.common.lib.enums.GenderEnum genderOf(Integer code) {
        if (code == null) return null;
        for (com.homi.common.lib.enums.GenderEnum item : com.homi.common.lib.enums.GenderEnum.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    private com.homi.common.lib.enums.IdTypeEnum idTypeOf(Integer code) {
        if (code == null) return null;
        for (com.homi.common.lib.enums.IdTypeEnum item : com.homi.common.lib.enums.IdTypeEnum.values()) {
            if (Objects.equals(item.getCode(), code)) {
                return item;
            }
        }
        return null;
    }

    private List<String> parseTags(String tags) {
        if (isBlank(tags)) {
            return new ArrayList<>();
        }
        return JSONUtil.toList(tags, String.class);
    }

    private void saveSettlementItems(OwnerCreateDTO dto, OwnerContract contract, OwnerContractSubject subject, List<OwnerSettlementItemDTO> items, Date now) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<OwnerSettlementItem> records = items.stream().map(item -> {
            OwnerSettlementItem record = new OwnerSettlementItem();
            record.setCompanyId(contract.getCompanyId());
            record.setContractId(contract.getId());
            record.setContractSubjectId(subject.getId());
            record.setFeeDirection(item.getFeeDirection());
            record.setFeeType(item.getFeeType());
            record.setItemName(item.getItemName());
            record.setTransferEnabled(Objects.requireNonNullElse(item.getTransferEnabled(), Boolean.FALSE));
            record.setTransferRatio(item.getTransferRatio());
            record.setSortOrder(Objects.requireNonNullElse(item.getSortOrder(), 0));
            record.setRemark(item.getRemark());
            record.setStatus(StatusEnum.ACTIVE.getValue());
            record.setCreateBy(dto.getCreateBy());
            record.setCreateTime(now);
            record.setUpdateBy(dto.getCreateBy());
            record.setUpdateTime(now);
            return record;
        }).toList();
        ownerSettlementItemRepo.saveBatch(records);
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

    private String resolveOwnerTag(Owner owner) {
        if (owner == null || owner.getOwnerType() == null || owner.getOwnerTypeId() == null) {
            return null;
        }
        List<String> tags;
        if (Objects.equals(owner.getOwnerType(), OwnerTypeEnum.PERSONAL.getCode())) {
            OwnerPersonal personal = ownerPersonalRepo.getById(owner.getOwnerTypeId());
            tags = personal == null ? List.of() : parseTags(personal.getTags());
        } else {
            OwnerCompany company = ownerCompanyRepo.getById(owner.getOwnerTypeId());
            tags = company == null ? List.of() : parseTags(company.getTags());
        }
        return tags.isEmpty() ? null : tags.getFirst();
    }

    private void saveLeaseFees(OwnerCreateDTO dto, Long contractId, Date now) {
        if (dto.getOwnerLeaseRule() == null || dto.getOwnerLeaseRule().getOtherFeeList() == null || dto.getOwnerLeaseRule().getOtherFeeList().isEmpty()) {
            return;
        }
        List<OwnerLeaseFee> records = dto.getOwnerLeaseRule().getOtherFeeList().stream().map(item -> {
            OwnerLeaseFee fee = new OwnerLeaseFee();
            fee.setCompanyId(dto.getOwnerContract().getCompanyId());
            fee.setContractId(contractId);
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
            fee.setCreateTime(now);
            fee.setUpdateBy(dto.getCreateBy());
            fee.setUpdateTime(now);
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

    private List<String> getFileUrls(Long bizId, String bizType) {
        if (bizId == null) return List.of();
        return fileAttachRepo.getFileAttachListByBizIdAndBizTypes(bizId, List.of(bizType))
            .stream()
            .map(FileAttach::getFileUrl)
            .filter(Objects::nonNull)
            .toList();
    }
}
