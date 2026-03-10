package com.homi.service.service.contract;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.contract.dto.seal.ContractSealCreateDTO;
import com.homi.model.contract.dto.seal.ContractSealQueryDTO;
import com.homi.model.contract.vo.seal.ContractSealVO;
import com.homi.model.dao.entity.ContractSeal;
import com.homi.model.dao.entity.ContractSealProvider;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.ContractSealProviderRepo;
import com.homi.model.dao.repo.ContractSealRepo;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractSealService {
    private final ContractSealRepo contractSealRepo;
    private final ContractSealProviderRepo contractSealProviderRepo;
    private final FileAttachRepo fileAttachRepo;
    private final UserRepo userRepo;

    public List<ContractSealVO> list(ContractSealQueryDTO queryDTO) {
        LambdaQueryWrapper<ContractSeal> wrapper = new LambdaQueryWrapper<ContractSeal>()
            .eq(ContractSeal::getCompanyId, queryDTO.getCompanyId());

        if (queryDTO.getSealType() != null) {
            wrapper.eq(ContractSeal::getSealType, queryDTO.getSealType());
        }

        if (queryDTO.getSource() != null) {
            wrapper.eq(ContractSeal::getSource, queryDTO.getSource());
        }

        if (queryDTO.getStatus() != null) {
            wrapper.eq(ContractSeal::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(ContractSeal::getUpdateTime, ContractSeal::getCreateTime, ContractSeal::getId);

        List<ContractSeal> list = contractSealRepo.list(wrapper);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream().map(item -> {
            ContractSealVO vo = BeanCopyUtils.copyBean(item, ContractSealVO.class);
            if (vo == null) {
                return null;
            }

            if (item.getOperatorId() != null) {
                User operator = userRepo.getBaseMapper().selectById(item.getOperatorId());
                if (operator != null) {
                    vo.setOperatorName(StringUtils.isNotBlank(operator.getRealName()) ? operator.getRealName() : operator.getNickname());
                    vo.setOperatorPhone(operator.getPhone());
                    vo.setOperatorIdType(operator.getIdType());
                    vo.setOperatorIdNo(operator.getIdNo());
                }
            }

            ContractSealProvider provider = contractSealProviderRepo.lambdaQuery()
                .eq(ContractSealProvider::getSealId, item.getId())
                .last("limit 1")
                .one();
            if (provider != null) {
                vo.setProviderAccountId(provider.getAccountId());
                vo.setProviderSealId(provider.getProviderSealId());
                vo.setAuthStatus(provider.getAuthStatus());
                vo.setAuthTime(provider.getAuthTime());
                vo.setExpireTime(provider.getExpireTime());
                vo.setProviderExtra(provider.getExtra());
            }

            List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(
                item.getId(), List.of(FileAttachBizTypeEnum.CONTRACT_SEAL_IMAGE.getBizType()));
            vo.setSealUrls(fileAttachList.stream().map(FileAttach::getFileUrl).toList());

            return vo;
        }).filter(Objects::nonNull).toList();
    }

    public Long createOrUpdate(ContractSealCreateDTO dto, Long companyId, Long userId) {
        validateCreate(dto);

        ContractSeal entity = ObjectUtil.defaultIfNull(dto.getId(), 0L) > 0
            ? contractSealRepo.getById(dto.getId())
            : new ContractSeal();

        if (entity == null) {
            throw new BizException("电子印章不存在");
        }

        entity.setCompanyId(companyId);
        entity.setSealType(dto.getSealType());
        entity.setSource(ObjectUtil.defaultIfNull(dto.getSource(), 1));
        entity.setCompanyName(dto.getCompanyName());
        entity.setCompanyUscc(dto.getCompanyUscc());
        entity.setLegalPerson(dto.getLegalPerson());
        entity.setLegalPersonIdType(dto.getLegalPersonIdType());
        entity.setLegalPersonIdNo(dto.getLegalPersonIdNo());
        entity.setOperatorId(dto.getOperatorId());
        entity.setStatus(ObjectUtil.defaultIfNull(dto.getStatus(), 1));
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(userId);
        entity.setUpdateTime(DateUtil.date());

        if (ObjectUtil.defaultIfNull(dto.getId(), 0L) <= 0) {
            entity.setCreateBy(userId);
            entity.setCreateTime(DateUtil.date());
            contractSealRepo.save(entity);
        } else {
            contractSealRepo.updateById(entity);
        }

        if (dto.getSealUrls() != null) {
            fileAttachRepo.recreateFileAttachList(entity.getId(), FileAttachBizTypeEnum.CONTRACT_SEAL_IMAGE.getBizType(), dto.getSealUrls());
        }

        handleProviderInfo(dto, entity.getId(), userId);

        return entity.getId();
    }

    private void validateCreate(ContractSealCreateDTO dto) {
        if (dto.getSealType() == null) {
            throw new BizException("印章类型不能为空");
        }

        if (dto.getSealType() == 1) {
            if (StringUtils.isBlank(dto.getCompanyName())) {
                throw new BizException("公司名称不能为空");
            }
            if (StringUtils.isBlank(dto.getCompanyUscc())) {
                throw new BizException("社会统一信用代码不能为空");
            }
            if (StringUtils.isBlank(dto.getLegalPerson())) {
                throw new BizException("法人姓名不能为空");
            }
            if (StringUtils.isBlank(dto.getLegalPersonIdNo())) {
                throw new BizException("法人证件号不能为空");
            }
        }

        if (dto.getSealType() == 2 && dto.getOperatorId() == null) {
            throw new BizException("请选择经办人");
        }
    }

    private void handleProviderInfo(ContractSealCreateDTO dto, Long sealId, Long userId) {
        if (StringUtils.isAllBlank(dto.getProviderAccountId(), dto.getProviderSealId(), dto.getProviderExtra())
            && dto.getAuthStatus() == null && dto.getAuthTime() == null && dto.getExpireTime() == null) {
            return;
        }

        ContractSealProvider provider = contractSealProviderRepo.lambdaQuery()
            .eq(ContractSealProvider::getSealId, sealId)
            .last("limit 1")
            .one();

        if (provider == null) {
            provider = new ContractSealProvider();
            provider.setSealId(sealId);
            provider.setCreateBy(userId);
            provider.setCreateTime(DateUtil.date());
        }

        provider.setAccountId(dto.getProviderAccountId());
        provider.setProviderSealId(dto.getProviderSealId());
        provider.setAuthStatus(dto.getAuthStatus());
        provider.setAuthTime(dto.getAuthTime());
        provider.setExpireTime(dto.getExpireTime());
        provider.setExtra(dto.getProviderExtra());
        provider.setUpdateBy(userId);
        provider.setUpdateTime(DateUtil.date());

        if (provider.getId() == null) {
            contractSealProviderRepo.save(provider);
        } else {
            contractSealProviderRepo.updateById(provider);
        }
    }
}
