package com.homi.service.service.tenant;

import cn.hutool.core.collection.CollUtil;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.TenantMate;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.TenantMateRepo;
import com.homi.model.dto.tenant.TenantMateDTO;
import com.homi.model.vo.tenant.TenantMateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 租客
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Service
@RequiredArgsConstructor
public class TenantMateService {
    private final TenantMateRepo tenantMateRepo;
    private final FileAttachRepo fileAttachRepo;

    /**
     * 保存租客同住人列表
     *
     * @param tenantId       租客id
     * @param tenantMateList 同住人列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveTenantMateList(Long tenantId, List<TenantMateDTO> tenantMateList) {
        tenantMateList.forEach(tenantMateDTO -> {
            TenantMate tenantMate = BeanCopyUtils.copyBean(tenantMateDTO, TenantMate.class);
            assert tenantMate != null;
            tenantMate.setTenantId(tenantId);

            tenantMateRepo.save(tenantMate);

            // 保存租客身份证反面
            if (CollUtil.isNotEmpty(tenantMateDTO.getIdCardBackList())) {
                fileAttachRepo.addFileAttachBatch(tenantMate.getId(), FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_BACK.getBizType(), tenantMateDTO.getIdCardBackList());
            }

            // 保存租客身份证正面
            if (CollUtil.isNotEmpty(tenantMateDTO.getIdCardFrontList())) {
                fileAttachRepo.addFileAttachBatch(tenantMate.getId(), FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_FRONT.getBizType(), tenantMateDTO.getIdCardFrontList());
            }

            // 保存租客手持照片
            if (CollUtil.isNotEmpty(tenantMateDTO.getIdCardInHandList())) {
                fileAttachRepo.addFileAttachBatch(tenantMate.getId(), FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_FRONT.getBizType(), tenantMateDTO.getIdCardInHandList());
            }

            // 保存租客其他照片
            if (CollUtil.isNotEmpty(tenantMateDTO.getOtherImageList())) {
                fileAttachRepo.addFileAttachBatch(tenantMate.getId(), FileAttachBizTypeEnum.TENANT_MATE_OTHER_IMAGE.getBizType(), tenantMateDTO.getOtherImageList());
            }
        });
    }

    public List<TenantMateVO> getTenantMateListByTenantId(Long tenantId) {
        return tenantMateRepo.lambdaQuery()
            .eq(TenantMate::getTenantId, tenantId)
            .list()
            .stream()
            .map(tenantMate -> {
                TenantMateVO tenantMateVO = BeanCopyUtils.copyBean(tenantMate, TenantMateVO.class);
                assert tenantMateVO != null;
                List<FileAttach> idCardBackUrls = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantMate.getId(), List.of(FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_BACK.getBizType()));
                tenantMateVO.setIdCardBackList(idCardBackUrls.stream().map(FileAttach::getFileUrl).toList());
                List<FileAttach> idCardFrontUrls = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantMate.getId(), List.of(FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_FRONT.getBizType()));
                tenantMateVO.setIdCardFrontList(idCardFrontUrls.stream().map(FileAttach::getFileUrl).toList());
                List<FileAttach> idCardInHandUrls = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantMate.getId(), List.of(FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_IN_HAND.getBizType()));
                tenantMateVO.setIdCardInHandList(idCardInHandUrls.stream().map(FileAttach::getFileUrl).toList());
                List<FileAttach> otherImageUrls = fileAttachRepo.getFileAttachListByBizIdAndBizTypes(tenantMate.getId(), List.of(FileAttachBizTypeEnum.TENANT_MATE_OTHER_IMAGE.getBizType()));
                tenantMateVO.setOtherImageList(otherImageUrls.stream().map(FileAttach::getFileUrl).toList());
                return tenantMateVO;
            })
            .toList();
    }
}
