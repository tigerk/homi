package com.homi.service.service.company;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.company.dto.digitalSign.CompanyDigitalSignCreateDTO;
import com.homi.model.company.dto.digitalSign.CompanyDigitalSignQueryDTO;
import com.homi.model.company.vo.digitalSign.CompanyDigitalSignVO;
import com.homi.model.dao.entity.CompanyDigitalSign;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.CompanyDigitalSignRepo;
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
public class CompanyDigitalSignService {
    private final CompanyDigitalSignRepo companyDigitalSignRepo;
    private final FileAttachRepo fileAttachRepo;
    private final UserRepo userRepo;

    public List<CompanyDigitalSignVO> list(CompanyDigitalSignQueryDTO queryDTO) {
        LambdaQueryWrapper<CompanyDigitalSign> wrapper = new LambdaQueryWrapper<CompanyDigitalSign>()
            .eq(CompanyDigitalSign::getCompanyId, queryDTO.getCompanyId());

        if (queryDTO.getSignType() != null) {
            wrapper.eq(CompanyDigitalSign::getSignType, queryDTO.getSignType());
        }

        wrapper.orderByDesc(CompanyDigitalSign::getUpdateTime, CompanyDigitalSign::getCreateTime, CompanyDigitalSign::getId);

        List<CompanyDigitalSign> list = companyDigitalSignRepo.list(wrapper);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream().map(item -> {
            CompanyDigitalSignVO vo = BeanCopyUtils.copyBean(item, CompanyDigitalSignVO.class);
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

            List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(
                item.getId(), List.of(FileAttachBizTypeEnum.COMPANY_DIGITAL_SIGN_SEAL.getBizType()));
            vo.setSealUrls(fileAttachList.stream().map(FileAttach::getFileUrl).toList());

            return vo;
        }).filter(Objects::nonNull).toList();
    }

    public Long createOrUpdate(CompanyDigitalSignCreateDTO dto, Long companyId, Long userId) {
        validateCreate(dto);

        CompanyDigitalSign entity = ObjectUtil.defaultIfNull(dto.getId(), 0L) > 0
            ? companyDigitalSignRepo.getById(dto.getId())
            : new CompanyDigitalSign();

        if (entity == null) {
            throw new BizException("电子签章不存在");
        }

        entity.setCompanyId(companyId);
        entity.setSignType(dto.getSignType());
        entity.setName(dto.getName());
        entity.setUscc(dto.getUscc());
        entity.setLegalPerson(dto.getLegalPerson());
        entity.setLegalPersonIdType(dto.getLegalPersonIdType());
        entity.setLegalPersonIdNo(dto.getLegalPersonIdNo());
        entity.setOperatorId(dto.getOperatorId());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(userId);
        entity.setUpdateTime(DateUtil.date());

        if (ObjectUtil.defaultIfNull(dto.getId(), 0L) <= 0) {
            entity.setCreateBy(userId);
            entity.setCreateTime(DateUtil.date());
            companyDigitalSignRepo.save(entity);
        } else {
            companyDigitalSignRepo.updateById(entity);
        }

        if (dto.getSealUrls() != null) {
            fileAttachRepo.recreateFileAttachList(entity.getId(), FileAttachBizTypeEnum.COMPANY_DIGITAL_SIGN_SEAL.getBizType(), dto.getSealUrls());
        }

        return entity.getId();
    }

    private void validateCreate(CompanyDigitalSignCreateDTO dto) {
        if (dto.getSignType() == null) {
            throw new BizException("签章类型不能为空");
        }

        if (dto.getSignType() == 1) {
            if (StringUtils.isBlank(dto.getName())) {
                throw new BizException("公司名称不能为空");
            }
            if (StringUtils.isBlank(dto.getUscc())) {
                throw new BizException("社会统一信用代码不能为空");
            }
            if (StringUtils.isBlank(dto.getLegalPerson())) {
                throw new BizException("法人姓名不能为空");
            }
            if (StringUtils.isBlank(dto.getLegalPersonIdNo())) {
                throw new BizException("法人证件号不能为空");
            }
        }

        if (dto.getSignType() == 2 && dto.getOperatorId() == null) {
            throw new BizException("请选择经办人");
        }
    }
}
