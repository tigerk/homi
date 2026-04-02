package com.homi.service.service.owner;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.contract.OwnerParamsEnum;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import com.homi.common.lib.enums.owner.OwnerSignStatusEnum;
import com.homi.common.lib.enums.owner.OwnerTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.Owner;
import com.homi.model.dao.entity.OwnerAccount;
import com.homi.model.dao.entity.OwnerCompany;
import com.homi.model.dao.entity.OwnerContract;
import com.homi.model.dao.entity.OwnerContractHouse;
import com.homi.model.dao.entity.OwnerLeaseFreeRule;
import com.homi.model.dao.entity.OwnerLeaseRule;
import com.homi.model.dao.entity.OwnerPersonal;
import com.homi.model.dao.entity.OwnerRentFreeRule;
import com.homi.model.dao.entity.OwnerSettlementRule;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.dao.repo.HouseRepo;
import com.homi.model.dao.repo.OwnerAccountRepo;
import com.homi.model.dao.repo.OwnerCompanyRepo;
import com.homi.model.dao.repo.OwnerContractHouseRepo;
import com.homi.model.dao.repo.OwnerContractRepo;
import com.homi.model.dao.repo.OwnerLeaseFreeRuleRepo;
import com.homi.model.dao.repo.OwnerLeaseRuleRepo;
import com.homi.model.dao.repo.OwnerPersonalRepo;
import com.homi.model.dao.repo.OwnerRentFreeRuleRepo;
import com.homi.model.dao.repo.OwnerRepo;
import com.homi.model.dao.repo.OwnerSettlementRuleRepo;
import com.homi.model.owner.dto.OwnerCompanyDTO;
import com.homi.model.owner.dto.OwnerContractDTO;
import com.homi.model.owner.dto.OwnerContractIdDTO;
import com.homi.model.owner.dto.OwnerContractStatusDTO;
import com.homi.model.owner.dto.OwnerContractHouseDTO;
import com.homi.model.owner.dto.OwnerCreateDTO;
import com.homi.model.owner.dto.OwnerLeaseFreeRuleDTO;
import com.homi.model.owner.dto.OwnerLeaseRuleDTO;
import com.homi.model.owner.dto.OwnerPersonalDTO;
import com.homi.model.owner.dto.OwnerQueryDTO;
import com.homi.model.owner.dto.OwnerRentFreeRuleDTO;
import com.homi.model.owner.dto.OwnerSettlementRuleDTO;
import com.homi.model.owner.dto.OwnerUpdateDTO;
import com.homi.model.owner.vo.OwnerDetailVO;
import com.homi.model.owner.vo.OwnerListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerContractService {
    private final OwnerRepo ownerRepo;
    private final OwnerPersonalRepo ownerPersonalRepo;
    private final OwnerCompanyRepo ownerCompanyRepo;
    private final OwnerContractRepo ownerContractRepo;
    private final OwnerContractHouseRepo ownerContractHouseRepo;
    private final OwnerSettlementRuleRepo ownerSettlementRuleRepo;
    private final OwnerRentFreeRuleRepo ownerRentFreeRuleRepo;
    private final OwnerLeaseRuleRepo ownerLeaseRuleRepo;
    private final OwnerLeaseFreeRuleRepo ownerLeaseFreeRuleRepo;
    private final OwnerAccountRepo ownerAccountRepo;
    private final ContractTemplateRepo contractTemplateRepo;
    private final HouseRepo houseRepo;

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
        contract.setStatus(Objects.requireNonNullElse(dto.getOwnerContract().getStatus(), StatusEnum.ACTIVE).getValue());
        contract.setApprovalStatus(Objects.requireNonNullElse(dto.getOwnerContract().getApprovalStatus(), BizApprovalStatusEnum.APPROVED).getCode());
        contract.setCreateBy(dto.getCreateBy());
        contract.setCreateTime(now);
        contract.setUpdateBy(dto.getCreateBy());
        contract.setUpdateTime(now);
        contract.setContractContent(buildContractContent(contract, ownerId, dto.getContractHouseList()));
        ownerContractRepo.save(contract);

        List<OwnerContractHouse> contractHouses = saveContractHouses(dto, contract.getId(), now);
        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            saveLightManagedRules(dto, contract, contractHouses, now);
        } else if (OwnerCooperationModeEnum.MASTER_LEASE.name().equals(contract.getCooperationMode())) {
            saveMasterLeaseRules(dto, contract.getId(), now);
        }
        initOwnerAccount(dto.getOwnerContract().getCompanyId(), ownerId, now);
        return contract.getId();
    }

    public PageVO<OwnerListVO> getOwnerContractList(OwnerQueryDTO query) {
        Page<OwnerContract> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        LambdaQueryWrapper<OwnerContract> wrapper = new LambdaQueryWrapper<>();
        List<Long> ownerIds = resolveOwnerIds(query);
        if (ownerIds != null && ownerIds.isEmpty()) {
            return emptyPage(query);
        }
        wrapper.in(ownerIds != null, OwnerContract::getOwnerId, ownerIds);
        wrapper.eq(Objects.nonNull(query.getStatus()), OwnerContract::getStatus, query.getStatus() == null ? null : query.getStatus().getValue());
        wrapper.eq(Objects.nonNull(query.getSignStatus()), OwnerContract::getSignStatus, query.getSignStatus() == null ? null : query.getSignStatus().getCode());
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

        List<OwnerContractHouse> contractHouses = ownerContractHouseRepo.listByContractId(contract.getId());
        List<OwnerContractHouseDTO> houseDTOList = contractHouses.stream().map(item -> {
            OwnerContractHouseDTO houseDTO = new OwnerContractHouseDTO();
            houseDTO.setId(item.getId());
            houseDTO.setHouseId(item.getHouseId());
            houseDTO.setHouseName(item.getHouseNameSnapshot());
            houseDTO.setRemark(item.getRemark());
            return houseDTO;
        }).collect(Collectors.toList());

        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            List<OwnerSettlementRule> settlementRules = ownerSettlementRuleRepo.list(
                new LambdaQueryWrapper<OwnerSettlementRule>().eq(OwnerSettlementRule::getContractId, contract.getId())
            );
            List<OwnerRentFreeRule> rentFreeRules = ownerRentFreeRuleRepo.list(
                new LambdaQueryWrapper<OwnerRentFreeRule>().eq(OwnerRentFreeRule::getContractId, contract.getId())
            );
            for (OwnerContractHouseDTO houseDTO : houseDTOList) {
                OwnerSettlementRule settlementRule = settlementRules.stream()
                    .filter(item -> Objects.equals(item.getContractHouseId(), houseDTO.getId()))
                    .findFirst()
                    .orElse(null);
                if (settlementRule != null) {
                    houseDTO.setSettlementRule(toOwnerSettlementRuleDTO(settlementRule));
                }
                OwnerRentFreeRule rentFreeRule = rentFreeRules.stream()
                    .filter(item -> Objects.equals(item.getContractHouseId(), houseDTO.getId()))
                    .findFirst()
                    .orElse(null);
                if (rentFreeRule != null) {
                    houseDTO.setRentFreeRule(toOwnerRentFreeRuleDTO(rentFreeRule));
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
        }
        vo.setContractHouseList(houseDTOList);
        vo.setCreateBy(contract.getCreateBy());
        vo.setCreateTime(contract.getCreateTime());
        vo.setUpdateBy(contract.getUpdateBy());
        vo.setUpdateTime(contract.getUpdateTime());
        return vo;
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
        OwnerContract contract = BeanCopyUtils.copyBean(dto.getOwnerContract(), OwnerContract.class);
        assert contract != null;
        contract.setOwnerId(owner.getId());
        contract.setCooperationMode(enumName(dto.getOwnerContract().getCooperationMode()));
        contract.setSignStatus(Objects.requireNonNullElse(dto.getOwnerContract().getSignStatus(), OwnerSignStatusEnum.PENDING).getCode());
        contract.setStatus(Objects.requireNonNullElse(dto.getOwnerContract().getStatus(), StatusEnum.ACTIVE).getValue());
        contract.setApprovalStatus(Objects.requireNonNullElse(dto.getOwnerContract().getApprovalStatus(), BizApprovalStatusEnum.APPROVED).getCode());
        contract.setUpdateBy(dto.getUpdateBy());
        contract.setUpdateTime(now);
        contract.setContractContent(buildContractContent(contract, owner.getId(), dto.getContractHouseList()));
        ownerContractRepo.updateById(contract);

        clearContractRelations(contract.getId());
        List<OwnerContractHouse> contractHouses = saveContractHouses(toCreateDTO(dto), contract.getId(), now);
        if (OwnerCooperationModeEnum.LIGHT_MANAGED.name().equals(contract.getCooperationMode())) {
            saveLightManagedRules(toCreateDTO(dto), contract, contractHouses, now);
        } else {
            saveMasterLeaseRules(toCreateDTO(dto), contract.getId(), now);
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

        ownerContractHouseRepo.listByContractId(contract.getId()).forEach(item -> {
            item.setStatus(dto.getStatus().getValue());
            item.setUpdateBy(updateBy);
            item.setUpdateTime(now);
            ownerContractHouseRepo.updateById(item);
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
        if (dto.getContractHouseList() == null || dto.getContractHouseList().isEmpty()) {
            throw new IllegalArgumentException("签约房源不能为空");
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
        if (dto.getContractHouseList() == null || dto.getContractHouseList().isEmpty()) {
            throw new IllegalArgumentException("签约房源不能为空");
        }
        OwnerCooperationModeEnum mode = dto.getOwnerContract().getCooperationMode();
        if (mode == null) {
            throw new IllegalArgumentException("合作模式不正确");
        }
        if (OwnerCooperationModeEnum.MASTER_LEASE.equals(mode) && dto.getOwnerLeaseRule() == null) {
            throw new IllegalArgumentException("包租规则不能为空");
        }
    }

    private List<OwnerContractHouse> saveContractHouses(OwnerCreateDTO dto, Long contractId, Date now) {
        List<OwnerContractHouse> records = dto.getContractHouseList().stream().map(item -> {
            House house = houseRepo.getById(item.getHouseId());
            if (house == null) {
                throw new IllegalArgumentException("房源不存在: " + item.getHouseId());
            }
            OwnerContractHouse record = new OwnerContractHouse();
            record.setCompanyId(dto.getOwnerContract().getCompanyId());
            record.setContractId(contractId);
            record.setHouseId(item.getHouseId());
            record.setHouseNameSnapshot(house.getHouseName());
            record.setRemark(item.getRemark());
            record.setStatus(1);
            record.setCreateBy(dto.getCreateBy());
            record.setCreateTime(now);
            record.setUpdateBy(dto.getCreateBy());
            record.setUpdateTime(now);
            return record;
        }).collect(Collectors.toList());
        ownerContractHouseRepo.saveBatch(records);
        return records;
    }

    private void saveLightManagedRules(OwnerCreateDTO dto, OwnerContract contract, List<OwnerContractHouse> contractHouses, Date now) {
        for (int i = 0; i < contractHouses.size(); i++) {
            OwnerContractHouse house = contractHouses.get(i);
            OwnerContractHouseDTO houseDTO = dto.getContractHouseList().get(i);
            OwnerSettlementRuleDTO settlementRuleDTO = houseDTO.getSettlementRule();
            if (settlementRuleDTO != null) {
                OwnerSettlementRule rule = new OwnerSettlementRule();
                BeanUtils.copyProperties(settlementRuleDTO, rule);
                rule.setCompanyId(contract.getCompanyId());
                rule.setContractId(contract.getId());
                rule.setContractHouseId(house.getId());
                rule.setRuleVersion(1);
                rule.setIncomeBasis(enumName(settlementRuleDTO.getIncomeBasis()));
                rule.setSettlementMode(enumName(settlementRuleDTO.getSettlementMode()));
                rule.setCommissionMode(enumName(settlementRuleDTO.getCommissionMode()));
                rule.setServiceFeeMode(enumName(settlementRuleDTO.getServiceFeeMode()));
                rule.setBearTaxType(enumName(settlementRuleDTO.getBearTaxType()));
                rule.setStatus(Objects.requireNonNullElse(settlementRuleDTO.getStatus(), StatusEnum.ACTIVE).getValue());
                rule.setRuleSnapshot(JSONUtil.toJsonStr(settlementRuleDTO));
                rule.setCreateBy(dto.getCreateBy());
                rule.setCreateTime(now);
                rule.setUpdateBy(dto.getCreateBy());
                rule.setUpdateTime(now);
                ownerSettlementRuleRepo.save(rule);
            }
            OwnerRentFreeRuleDTO rentFreeRuleDTO = houseDTO.getRentFreeRule();
            if (rentFreeRuleDTO != null) {
                OwnerRentFreeRule rule = new OwnerRentFreeRule();
                BeanUtils.copyProperties(rentFreeRuleDTO, rule);
                rule.setCompanyId(contract.getCompanyId());
                rule.setContractId(contract.getId());
                rule.setContractHouseId(house.getId());
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
            personal.setTags(JSONUtil.toJsonStr(personalDTO.getTags()));
            personal.setStatus(Objects.requireNonNullElse(personalDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            personal.setCreateBy(dto.getCreateBy());
            personal.setCreateTime(now);
            personal.setUpdateBy(dto.getCreateBy());
            personal.setUpdateTime(now);
            ownerPersonalRepo.save(personal);
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
            company.setTags(JSONUtil.toJsonStr(companyDTO.getTags()));
            company.setStatus(Objects.requireNonNullElse(companyDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            company.setCreateBy(dto.getCreateBy());
            company.setCreateTime(now);
            company.setUpdateBy(dto.getCreateBy());
            company.setUpdateTime(now);
            ownerCompanyRepo.save(company);
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
            personal.setTags(JSONUtil.toJsonStr(personalDTO.getTags()));
            personal.setStatus(Objects.requireNonNullElse(personalDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            personal.setUpdateBy(dto.getUpdateBy());
            personal.setUpdateTime(now);
            if (personal.getId() == null) {
                ownerPersonalRepo.save(personal);
            } else {
                ownerPersonalRepo.updateById(personal);
            }
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
            company.setTags(JSONUtil.toJsonStr(companyDTO.getTags()));
            company.setStatus(Objects.requireNonNullElse(companyDTO.getStatus(), StatusEnum.ACTIVE).getValue());
            company.setUpdateBy(dto.getUpdateBy());
            company.setUpdateTime(now);
            if (company.getId() == null) {
                ownerCompanyRepo.save(company);
            } else {
                ownerCompanyRepo.updateById(company);
            }
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
        ownerSettlementRuleRepo.remove(new LambdaQueryWrapper<OwnerSettlementRule>().eq(OwnerSettlementRule::getContractId, contractId));
        ownerRentFreeRuleRepo.remove(new LambdaQueryWrapper<OwnerRentFreeRule>().eq(OwnerRentFreeRule::getContractId, contractId));
        ownerLeaseRuleRepo.remove(new LambdaQueryWrapper<OwnerLeaseRule>().eq(OwnerLeaseRule::getContractId, contractId));
        ownerLeaseFreeRuleRepo.remove(new LambdaQueryWrapper<OwnerLeaseFreeRule>().eq(OwnerLeaseFreeRule::getContractId, contractId));
        ownerContractHouseRepo.remove(new LambdaQueryWrapper<OwnerContractHouse>().eq(OwnerContractHouse::getContractId, contractId));
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

    private String buildContractContent(OwnerContract contract, Long ownerId, List<OwnerContractHouseDTO> houseDTOs) {
        ContractTemplate template = contractTemplateRepo.getById(contract.getContractTemplateId());
        if (template == null || template.getTemplateContent() == null) {
            return contract.getContractContent();
        }
        Owner owner = ownerRepo.getById(ownerId);
        List<House> houses = houseDTOs.stream().map(item -> houseRepo.getById(item.getHouseId())).filter(Objects::nonNull).toList();
        String content = template.getTemplateContent();
        content = content.replace(OwnerParamsEnum.CONTRACT_NUMBER.getKey(), contract.getContractNo());
        content = content.replace(OwnerParamsEnum.HOUSE_ADDRESS.getKey(), houses.stream().map(this::formatHouseAddress).collect(Collectors.joining("；")));
        content = content.replace(OwnerParamsEnum.PROJECT_NAME.getKey(), houses.stream().map(House::getHouseName).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.BUILDING_NUMBER.getKey(), houses.stream().map(House::getBuilding).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.UNIT_NUMBER.getKey(), houses.stream().map(House::getUnit).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.HOUSE_NUMBER.getKey(), houses.stream().map(House::getDoorNumber).filter(Objects::nonNull).collect(Collectors.joining("，")));
        content = content.replace(OwnerParamsEnum.SHARED_ROOM_NUMBER.getKey(), "");
        content = content.replace(OwnerParamsEnum.SIGNED_HOUSE_LIST.getKey(), houses.stream().map(House::getHouseName).filter(Objects::nonNull).collect(Collectors.joining("，")));
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

        ContractTemplate template = contractTemplateRepo.getById(contract.getContractTemplateId());
        if (template != null) {
            vo.setContractTemplateName(template.getTemplateName());
        }
        List<OwnerContractHouse> contractHouses = ownerContractHouseRepo.listByContractId(contract.getId());
        vo.setHouseNames(contractHouses.stream().map(OwnerContractHouse::getHouseNameSnapshot).collect(Collectors.joining("，")));
        return vo;
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private OwnerCreateDTO toCreateDTO(OwnerUpdateDTO dto) {
        OwnerCreateDTO createDTO = new OwnerCreateDTO();
        createDTO.setOwnerType(dto.getOwnerType());
        createDTO.setOwnerPersonal(dto.getOwnerPersonal());
        createDTO.setOwnerCompany(dto.getOwnerCompany());
        createDTO.setOwnerContract(dto.getOwnerContract());
        createDTO.setContractHouseList(dto.getContractHouseList());
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
        dto.setCommissionMode(rule.getCommissionMode() == null ? null : com.homi.common.lib.enums.owner.OwnerFeeModeEnum.valueOf(rule.getCommissionMode()));
        dto.setCommissionValue(rule.getCommissionValue());
        dto.setServiceFeeMode(rule.getServiceFeeMode() == null ? null : com.homi.common.lib.enums.owner.OwnerFeeModeEnum.valueOf(rule.getServiceFeeMode()));
        dto.setServiceFeeValue(rule.getServiceFeeValue());
        dto.setBearTaxType(rule.getBearTaxType() == null ? null : com.homi.common.lib.enums.owner.OwnerBearTypeEnum.valueOf(rule.getBearTaxType()));
        dto.setEffectiveStart(rule.getEffectiveStart());
        dto.setEffectiveEnd(rule.getEffectiveEnd());
        dto.setStatus(statusOf(rule.getStatus()));
        dto.setRemark(rule.getRemark());
        return dto;
    }

    private OwnerRentFreeRuleDTO toOwnerRentFreeRuleDTO(OwnerRentFreeRule rule) {
        OwnerRentFreeRuleDTO dto = new OwnerRentFreeRuleDTO();
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
        dto.setBillingStart(rule.getBillingStart());
        dto.setBillingEnd(rule.getBillingEnd());
        dto.setProrateType(rule.getProrateType() == null ? null : com.homi.common.lib.enums.owner.OwnerProrateTypeEnum.valueOf(rule.getProrateType()));
        dto.setStatus(statusOf(rule.getStatus()));
        dto.setRemark(rule.getRemark());
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
}
