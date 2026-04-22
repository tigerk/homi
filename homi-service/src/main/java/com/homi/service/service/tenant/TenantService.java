package com.homi.service.service.tenant;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantPersonal;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.TenantCompanyRepo;
import com.homi.model.dao.repo.TenantPersonalRepo;
import com.homi.model.dao.repo.TenantRepo;
import com.homi.model.tenant.vo.TenantCompanyVO;
import com.homi.model.tenant.vo.TenantPersonalVO;
import com.homi.model.tenant.vo.TenantProfileSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepo tenantRepo;
    private final TenantPersonalRepo tenantPersonalRepo;
    private final TenantCompanyRepo tenantCompanyRepo;
    private final FileAttachRepo fileAttachRepo;

    public Tenant getTenant(Long tenantId) {
        return tenantId == null ? null : tenantRepo.getById(tenantId);
    }

    public TenantPersonal getPersonalDetail(Tenant tenant) {
        return tenant.getTenantTypeId() == null ? null : tenantPersonalRepo.getById(tenant.getTenantTypeId());
    }

    public TenantCompany getCompanyDetail(Tenant tenant) {
        return tenant.getTenantTypeId() == null ? null : tenantCompanyRepo.getById(tenant.getTenantTypeId());
    }

    public List<TenantProfileSearchVO> searchTenantProfiles(String keyword, Integer tenantType, Integer limit) {
        if (CharSequenceUtil.isBlank(keyword)) {
            return List.of();
        }

        return tenantRepo.searchTenantList(keyword, tenantType, limit).stream().map(this::buildTenantProfileSearchVO).toList();
    }

    private TenantProfileSearchVO buildTenantProfileSearchVO(Tenant tenant) {
        TenantProfileSearchVO vo = new TenantProfileSearchVO();
        vo.setTenantId(tenant.getId());
        vo.setTenantType(tenant.getTenantType());
        vo.setTenantName(tenant.getTenantName());
        vo.setTenantPhone(tenant.getTenantPhone());
        vo.setUpdateAt(tenant.getUpdateAt() != null ? tenant.getUpdateAt() : tenant.getCreateAt());

        if (Objects.equals(tenant.getTenantType(), TenantTypeEnum.PERSONAL.getCode())) {
            vo.setTenantPersonal(buildTenantPersonalVO(tenant.getTenantTypeId()));
        } else if (Objects.equals(tenant.getTenantType(), TenantTypeEnum.ENTERPRISE.getCode())) {
            vo.setTenantCompany(buildTenantCompanyVO(tenant.getTenantTypeId()));
        }

        return vo;
    }

    private TenantPersonalVO buildTenantPersonalVO(Long tenantTypeId) {
        TenantPersonalVO tenantPersonalVO = tenantPersonalRepo.getTenantById(tenantTypeId);
        if (tenantPersonalVO == null) {
            return null;
        }

        List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantPersonalVO.getId(), ListUtil.of(
            FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType(),
            FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(),
            FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(),
            FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType()
        ));

        tenantPersonalVO.setOtherImageList(new ArrayList<>());
        tenantPersonalVO.setIdCardBackList(new ArrayList<>());
        tenantPersonalVO.setIdCardFrontList(new ArrayList<>());
        tenantPersonalVO.setIdCardInHandList(new ArrayList<>());

        fileAttachList.forEach(fileAttach -> {
            if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType())) {
                tenantPersonalVO.getOtherImageList().add(fileAttach.getFileUrl());
            } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType())) {
                tenantPersonalVO.getIdCardBackList().add(fileAttach.getFileUrl());
            } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType())) {
                tenantPersonalVO.getIdCardFrontList().add(fileAttach.getFileUrl());
            } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_ID_CARD_IN_HAND.getBizType())) {
                tenantPersonalVO.getIdCardInHandList().add(fileAttach.getFileUrl());
            }
        });

        // 兼容历史错误数据：早期“手持身份证照片”被错误存成 TENANT_ID_CARD_FRONT。
        if (tenantPersonalVO.getIdCardInHandList().isEmpty() && tenantPersonalVO.getIdCardFrontList().size() > 1) {
            while (tenantPersonalVO.getIdCardFrontList().size() > 1) {
                tenantPersonalVO.getIdCardInHandList().add(tenantPersonalVO.getIdCardFrontList().remove(tenantPersonalVO.getIdCardFrontList().size() - 1));
            }
        }

        return tenantPersonalVO;
    }

    private TenantCompanyVO buildTenantCompanyVO(Long tenantTypeId) {
        TenantCompanyVO tenantCompanyVO = tenantCompanyRepo.getTenantCompanyById(tenantTypeId);
        if (tenantCompanyVO == null) {
            return null;
        }

        List<FileAttach> fileAttachList = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantCompanyVO.getId(), ListUtil.of(
            FileAttachBizTypeEnum.BUSINESS_LICENSE.getBizType(),
            FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType()
        ));

        tenantCompanyVO.setOtherImageList(new ArrayList<>());
        tenantCompanyVO.setBusinessLicenseList(new ArrayList<>());

        fileAttachList.forEach(fileAttach -> {
            if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.BUSINESS_LICENSE.getBizType())) {
                tenantCompanyVO.getBusinessLicenseList().add(fileAttach.getFileUrl());
            } else if (Objects.equals(fileAttach.getBizType(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType())) {
                tenantCompanyVO.getOtherImageList().add(fileAttach.getFileUrl());
            }
        });

        return tenantCompanyVO;
    }
}
