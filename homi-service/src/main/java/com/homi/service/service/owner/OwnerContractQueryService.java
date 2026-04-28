package com.homi.service.service.owner;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.GenderEnum;
import com.homi.common.lib.enums.IdTypeEnum;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.owner.OwnerBearTypeEnum;
import com.homi.common.lib.enums.owner.OwnerContractMediumEnum;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import com.homi.common.lib.enums.owner.OwnerFeeModeEnum;
import com.homi.common.lib.enums.owner.OwnerFreeCalcModeEnum;
import com.homi.common.lib.enums.owner.OwnerFreeTypeEnum;
import com.homi.common.lib.enums.owner.OwnerIncomeBasisEnum;
import com.homi.common.lib.enums.owner.OwnerPaymentFeeBearTypeEnum;
import com.homi.common.lib.enums.owner.OwnerProrateTypeEnum;
import com.homi.common.lib.enums.owner.OwnerSettlementModeEnum;
import com.homi.common.lib.enums.owner.OwnerSettlementTimingEnum;
import com.homi.common.lib.enums.owner.OwnerSignStatusEnum;
import com.homi.common.lib.enums.owner.OwnerSignTypeEnum;
import com.homi.common.lib.enums.owner.OwnerTypeEnum;
import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.entity.OwnerCompany;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.entity.OwnerContractSubject;
import com.homi.model.dao.entity.OwnerLeaseFee;
import com.homi.model.dao.entity.OwnerLeaseFreeRule;
import com.homi.model.dao.entity.OwnerLeaseRule;
import com.homi.model.dao.entity.OwnerPersonal;
import com.homi.model.dao.entity.OwnerRentFreeRule;
import com.homi.model.dao.entity.OwnerSettlementFee;
import com.homi.model.dao.entity.OwnerSettlementRule;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.HouseRepo;
import com.homi.model.dao.repo.OwnerCompanyRepo;
import com.homi.model.dao.repo.OwnerContractRepo;
import com.homi.model.dao.repo.OwnerContractSubjectRepo;
import com.homi.model.dao.repo.OwnerLeaseFeeRepo;
import com.homi.model.dao.repo.OwnerLeaseFreeRuleRepo;
import com.homi.model.dao.repo.OwnerLeaseRuleRepo;
import com.homi.model.dao.repo.OwnerPersonalRepo;
import com.homi.model.dao.repo.OwnerRentFreeRuleRepo;
import com.homi.model.dao.repo.OwnerRepo;
import com.homi.model.dao.repo.OwnerSettlementFeeRepo;
import com.homi.model.dao.repo.OwnerSettlementRuleRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.owner.dto.OwnerCompanyDTO;
import com.homi.model.owner.dto.OwnerContractDTO;
import com.homi.model.owner.dto.OwnerContractIdDTO;
import com.homi.model.owner.dto.OwnerContractSubjectDTO;
import com.homi.model.owner.dto.OwnerLeaseFeeDTO;
import com.homi.model.owner.dto.OwnerLeaseFreeRuleDTO;
import com.homi.model.owner.dto.OwnerLeaseRuleDTO;
import com.homi.model.owner.dto.OwnerPersonalDTO;
import com.homi.model.owner.dto.OwnerQueryDTO;
import com.homi.model.owner.dto.OwnerRentFreeRuleDTO;
import com.homi.model.owner.dto.OwnerSettlementFeeDTO;
import com.homi.model.owner.dto.OwnerSettlementRuleDTO;
import com.homi.model.owner.vo.OwnerContractTotalVO;
import com.homi.model.owner.vo.OwnerDetailVO;
import com.homi.model.owner.vo.OwnerListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 业主合同查询服务。
 * <p>
 * 只承接列表、统计、详情和合同预览等读操作，避免查询逻辑进入写操作服务。
 */
@Service
@RequiredArgsConstructor
public class OwnerContractQueryService {
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
    private final ContractTemplateRepo contractTemplateRepo;
    private final HouseRepo houseRepo;
    private final UserRepo userRepo;
    private final FileAttachRepo fileAttachRepo;
    private final OwnerBillingGenerateService ownerBillingGenerateService;

    public PageVO<OwnerListVO> getOwnerContractList(OwnerQueryDTO query) {
        Page<OwnerContract> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        LambdaQueryWrapper<OwnerContract> wrapper = buildOwnerContractWrapper(query, false);
        List<Long> ownerIds = resolveOwnerIds(query);
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyPage(query);
        }
        wrapper.orderByDesc(OwnerContract::getCreateAt);
        Page<OwnerContract> result = ownerContractRepo.page(page, wrapper);

        List<OwnerListVO> list = result.getRecords().stream().map(this::toListVO).filter(Objects::nonNull).toList();
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
        vo.setOwnerType(OwnerTypeEnum.fromCode(owner.getOwnerType()));
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
            subjectDTO.setSubjectType(com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum.fromCode(item.getSubjectType()));
            subjectDTO.setSubjectId(item.getSubjectId());
            subjectDTO.setSubjectName(item.getSubjectNameSnapshot());
            subjectDTO.setRemark(item.getRemark());
            return subjectDTO;
        }).collect(Collectors.toList());

        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            appendLightManagedDetail(contract, subjectDTOList);
        } else {
            appendMasterLeaseDetail(contract, vo);
        }
        vo.setContractSubjectList(subjectDTOList);
        ContractSubjectSummary summary = buildContractSubjectSummary(contract, contractSubjects);
        vo.setSubjectCount(summary.subjectCount());
        vo.setTotalArea(summary.totalArea());
        vo.setConfiguredSubjectCount(summary.configuredSubjectCount());
        vo.setCreateBy(contract.getCreateBy());
        vo.setCreateAt(contract.getCreateAt());
        vo.setUpdateBy(contract.getUpdateBy());
        vo.setUpdateAt(contract.getUpdateAt());
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

    private void appendLightManagedDetail(OwnerContract contract, List<OwnerContractSubjectDTO> subjectDTOList) {
        List<OwnerSettlementRule> settlementRules = ownerSettlementRuleRepo.list(
            new LambdaQueryWrapper<OwnerSettlementRule>().eq(OwnerSettlementRule::getContractId, contract.getId())
        );
        List<OwnerRentFreeRule> rentFreeRules = ownerRentFreeRuleRepo.list(
            new LambdaQueryWrapper<OwnerRentFreeRule>().eq(OwnerRentFreeRule::getContractId, contract.getId())
        );
        for (OwnerContractSubjectDTO subjectDTO : subjectDTOList) {
            settlementRules.stream()
                .filter(item -> Objects.equals(item.getContractSubjectId(), subjectDTO.getId()))
                .findFirst()
                .map(this::toOwnerSettlementRuleDTO)
                .ifPresent(subjectDTO::setSettlementRule);
            rentFreeRules.stream()
                .filter(item -> Objects.equals(item.getContractSubjectId(), subjectDTO.getId()))
                .findFirst()
                .map(this::toOwnerRentFreeRuleDTO)
                .ifPresent(subjectDTO::setRentFreeRule);
        }
    }

    private void appendMasterLeaseDetail(OwnerContract contract, OwnerDetailVO vo) {
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

    private OwnerListVO toListVO(OwnerContract contract) {
        Owner owner = ownerRepo.getById(contract.getOwnerId());
        if (owner == null) {
            return null;
        }
        OwnerListVO vo = new OwnerListVO();
        vo.setContractId(contract.getId());
        vo.setOwnerId(owner.getId());
        vo.setOwnerType(OwnerTypeEnum.fromCode(owner.getOwnerType()));
        vo.setOwnerName(owner.getOwnerName());
        vo.setOwnerPhone(owner.getOwnerPhone());
        vo.setContractNo(contract.getContractNo());
        vo.setContractStart(contract.getContractStart());
        vo.setContractEnd(contract.getContractEnd());
        vo.setCooperationMode(contract.getCooperationMode() == null ? null : OwnerCooperationModeEnum.valueOf(contract.getCooperationMode()));
        vo.setSignStatus(OwnerSignStatusEnum.fromCode(contract.getSignStatus()));
        vo.setStatus(StatusEnum.fromValue(contract.getStatus()));
        vo.setContractNature(contract.getContractNature());
        vo.setCheckoutStatus(contract.getCheckoutStatus());
        vo.setCheckoutDate(contract.getCheckoutDate());
        vo.setCheckoutReason(contract.getCheckoutReason());
        vo.setCreateAt(contract.getCreateAt());
        vo.setUpdateAt(contract.getUpdateAt());

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
            .filter(item -> com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum.HOUSE.getCode().equals(item.getSubjectType()))
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

    private OwnerContractDTO toOwnerContractDTO(OwnerContract contract) {
        OwnerContractDTO dto = new OwnerContractDTO();
        dto.setId(contract.getId());
        dto.setCompanyId(contract.getCompanyId());
        dto.setOwnerId(contract.getOwnerId());
        dto.setCooperationMode(contract.getCooperationMode() == null ? null : OwnerCooperationModeEnum.valueOf(contract.getCooperationMode()));
        dto.setContractNo(contract.getContractNo());
        dto.setContractTemplateId(contract.getContractTemplateId());
        dto.setContractContent(contract.getContractContent());
        dto.setSignStatus(OwnerSignStatusEnum.fromCode(contract.getSignStatus()));
        dto.setSignType(contract.getSignType() == null ? null : OwnerSignTypeEnum.valueOf(contract.getSignType()));
        dto.setContractMedium(contract.getContractMedium() == null ? null : OwnerContractMediumEnum.valueOf(contract.getContractMedium()));
        dto.setNotifyOwner(Objects.requireNonNullElse(contract.getNotifyOwner(), Boolean.FALSE));
        dto.setContractStart(contract.getContractStart());
        dto.setContractEnd(contract.getContractEnd());
        dto.setStatus(StatusEnum.fromValue(contract.getStatus()));
        dto.setApprovalStatus(BizApprovalStatusEnum.getByCode(contract.getApprovalStatus()));
        dto.setRemark(contract.getRemark());
        dto.setParentContractId(contract.getParentContractId());
        dto.setContractNature(contract.getContractNature());
        dto.setRenewFromContractNo(contract.getRenewFromContractNo());
        dto.setCheckoutStatus(contract.getCheckoutStatus());
        dto.setCheckoutDate(contract.getCheckoutDate());
        dto.setCheckoutReason(contract.getCheckoutReason());
        dto.setCheckoutBy(contract.getCheckoutBy());
        dto.setCheckoutByName(contract.getCheckoutByName());
        dto.setCheckoutAt(contract.getCheckoutAt());
        dto.setCreateBy(contract.getCreateBy());
        dto.setCreateAt(contract.getCreateAt());
        dto.setUpdateBy(contract.getUpdateBy());
        dto.setUpdateAt(contract.getUpdateAt());
        return dto;
    }

    private OwnerPersonalDTO toOwnerPersonalDTO(OwnerPersonal personal) {
        OwnerPersonalDTO dto = new OwnerPersonalDTO();
        dto.setId(personal.getId());
        dto.setCompanyId(personal.getCompanyId());
        dto.setName(personal.getName());
        dto.setGender(GenderEnum.fromCode(personal.getGender()));
        dto.setIdType(IdTypeEnum.fromCode(personal.getIdType()));
        dto.setIdNo(personal.getIdNo());
        dto.setPhone(personal.getPhone());
        dto.setPayeeName(personal.getPayeeName());
        dto.setPayeePhone(personal.getPayeePhone());
        dto.setPayeeIdType(IdTypeEnum.fromCode(personal.getPayeeIdType()));
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
        dto.setStatus(StatusEnum.fromValue(personal.getStatus()));
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
        dto.setLegalPersonIdType(IdTypeEnum.fromCode(company.getLegalPersonIdType()));
        dto.setLegalPersonIdNo(company.getLegalPersonIdNo());
        dto.setContactName(company.getContactName());
        dto.setContactPhone(company.getContactPhone());
        dto.setPayeeName(company.getPayeeName());
        dto.setPayeePhone(company.getPayeePhone());
        dto.setPayeeIdType(IdTypeEnum.fromCode(company.getPayeeIdType()));
        dto.setPayeeIdNo(company.getPayeeIdNo());
        dto.setBankAccountName(company.getBankAccountName());
        dto.setBankAccountNo(company.getBankAccountNo());
        dto.setBankName(company.getBankName());
        dto.setBusinessLicenseUrls(getFileUrls(company.getId(), FileAttachBizTypeEnum.OWNER_BUSINESS_LICENSE.getBizType()));
        dto.setRegisteredAddress(company.getRegisteredAddress());
        dto.setTags(parseTags(company.getTags()));
        dto.setRemark(company.getRemark());
        dto.setStatus(StatusEnum.fromValue(company.getStatus()));
        dto.setCreateBy(company.getCreateBy());
        return dto;
    }

    private OwnerSettlementRuleDTO toOwnerSettlementRuleDTO(OwnerSettlementRule rule) {
        OwnerSettlementRuleDTO dto = new OwnerSettlementRuleDTO();
        dto.setIncomeBasis(rule.getIncomeBasis() == null ? null : OwnerIncomeBasisEnum.valueOf(rule.getIncomeBasis()));
        dto.setSettlementMode(rule.getSettlementMode() == null ? null : OwnerSettlementModeEnum.valueOf(rule.getSettlementMode()));
        dto.setGuaranteedRentAmount(rule.getGuaranteedRentAmount());
        dto.setHasGuaranteedRent(Objects.requireNonNullElse(rule.getHasGuaranteedRent(), Boolean.FALSE));
        dto.setCommissionMode(rule.getCommissionMode() == null ? null : OwnerFeeModeEnum.valueOf(rule.getCommissionMode()));
        dto.setCommissionValue(rule.getCommissionValue());
        dto.setServiceFeeMode(rule.getServiceFeeMode() == null ? null : OwnerFeeModeEnum.valueOf(rule.getServiceFeeMode()));
        dto.setServiceFeeValue(rule.getServiceFeeValue());
        dto.setManagementFeeEnabled(Objects.requireNonNullElse(rule.getManagementFeeEnabled(), Boolean.FALSE));
        dto.setManagementFeeMode(rule.getManagementFeeMode() == null ? null : OwnerFeeModeEnum.valueOf(rule.getManagementFeeMode()));
        dto.setManagementFeeValue(rule.getManagementFeeValue());
        dto.setBearTaxType(rule.getBearTaxType() == null ? null : OwnerBearTypeEnum.valueOf(rule.getBearTaxType()));
        dto.setPaymentFeeBearType(rule.getPaymentFeeBearType() == null ? null : OwnerPaymentFeeBearTypeEnum.valueOf(rule.getPaymentFeeBearType()));
        dto.setSettlementTiming(rule.getSettlementTiming() == null ? null : OwnerSettlementTimingEnum.valueOf(rule.getSettlementTiming()));
        dto.setRentFreeEnabled(Objects.requireNonNullElse(rule.getRentFreeEnabled(), Boolean.FALSE));
        dto.setSettlementItemList(ownerSettlementFeeRepo.list(new LambdaQueryWrapper<OwnerSettlementFee>()
                .eq(OwnerSettlementFee::getContractId, rule.getContractId())
                .eq(OwnerSettlementFee::getContractSubjectId, rule.getContractSubjectId()))
            .stream()
            .map(this::toOwnerSettlementFeeDTO)
            .toList());
        dto.setEffectiveStart(rule.getEffectiveStart());
        dto.setEffectiveEnd(rule.getEffectiveEnd());
        dto.setStatus(StatusEnum.fromValue(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        return dto;
    }

    private OwnerRentFreeRuleDTO toOwnerRentFreeRuleDTO(OwnerRentFreeRule rule) {
        OwnerRentFreeRuleDTO dto = new OwnerRentFreeRuleDTO();
        dto.setEnabled(Objects.requireNonNullElse(rule.getEnabled(), Boolean.FALSE));
        dto.setFreeType(rule.getFreeType() == null ? null : OwnerFreeTypeEnum.valueOf(rule.getFreeType()));
        dto.setStartDate(rule.getStartDate());
        dto.setEndDate(rule.getEndDate());
        dto.setBearType(rule.getBearType() == null ? null : OwnerBearTypeEnum.valueOf(rule.getBearType()));
        dto.setOwnerRatio(rule.getOwnerRatio());
        dto.setPlatformRatio(rule.getPlatformRatio());
        dto.setCalcMode(rule.getCalcMode() == null ? null : OwnerFreeCalcModeEnum.valueOf(rule.getCalcMode()));
        dto.setStatus(StatusEnum.fromValue(rule.getStatus()));
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
        dto.setProrateType(rule.getProrateType() == null ? null : OwnerProrateTypeEnum.valueOf(rule.getProrateType()));
        dto.setStatus(StatusEnum.fromValue(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        dto.setOtherFeeList(ownerLeaseFeeRepo.list(new LambdaQueryWrapper<OwnerLeaseFee>().eq(OwnerLeaseFee::getContractId, rule.getContractId()))
            .stream()
            .map(this::toOwnerLeaseFeeDTO)
            .toList());
        return dto;
    }

    private OwnerLeaseFreeRuleDTO toOwnerLeaseFreeRuleDTO(OwnerLeaseFreeRule rule) {
        OwnerLeaseFreeRuleDTO dto = new OwnerLeaseFreeRuleDTO();
        dto.setFreeType(rule.getFreeType() == null ? null : OwnerFreeTypeEnum.valueOf(rule.getFreeType()));
        dto.setStartDate(rule.getStartDate());
        dto.setEndDate(rule.getEndDate());
        dto.setCalcMode(rule.getCalcMode() == null ? null : OwnerFreeCalcModeEnum.valueOf(rule.getCalcMode()));
        dto.setFreeAmount(rule.getFreeAmount());
        dto.setFreeRatio(rule.getFreeRatio());
        dto.setStatus(StatusEnum.fromValue(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        return dto;
    }

    private OwnerSettlementFeeDTO toOwnerSettlementFeeDTO(OwnerSettlementFee item) {
        OwnerSettlementFeeDTO dto = new OwnerSettlementFeeDTO();
        dto.setFeeDirection(item.getFeeDirection());
        dto.setFeeType(item.getFeeType());
        dto.setDictDataId(item.getDictDataId());
        dto.setFeeName(item.getFeeName());
        dto.setTransferEnabled(Objects.requireNonNullElse(item.getTransferEnabled(), Boolean.FALSE));
        dto.setTransferRatio(item.getTransferRatio());
        dto.setSortOrder(item.getSortOrder());
        dto.setRemark(item.getRemark());
        return dto;
    }

    private OwnerLeaseFeeDTO toOwnerLeaseFeeDTO(OwnerLeaseFee fee) {
        OwnerLeaseFeeDTO dto = new OwnerLeaseFeeDTO();
        dto.setDictDataId(fee.getDictDataId());
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

    private List<String> parseTags(String tags) {
        if (isBlank(tags)) {
            return new ArrayList<>();
        }
        return JSONUtil.toList(tags, String.class);
    }

    private List<String> getFileUrls(Long bizId, String bizType) {
        if (bizId == null) {
            return List.of();
        }
        return fileAttachRepo.getFileAttachListByBizIdAndBizTypes(bizId, List.of(bizType))
            .stream()
            .map(FileAttach::getFileUrl)
            .filter(Objects::nonNull)
            .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
