package com.homi.service.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.TenantMate;
import com.homi.model.dao.repo.FileAttachRepo;
import com.homi.model.dao.repo.TenantMateRepo;
import com.homi.model.tenant.dto.TenantMateDTO;
import com.homi.model.tenant.vo.TenantMateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        if (CollUtil.isEmpty(tenantMateList)) {
            return;
        }

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

    /**
     * 处理同住人变更（差异化更新）
     */
    public void handleTenantMateUpdate(Long tenantId, List<TenantMateDTO> newMateList, List<TenantMateVO> originalMateList) {

        if (newMateList == null) {
            newMateList = new ArrayList<>();
        }
        if (originalMateList == null) {
            originalMateList = new ArrayList<>();
        }

        // 构建原始同住人ID映射
        Map<Integer, TenantMateVO> originalMateMap = originalMateList.stream()
            .collect(Collectors.toMap(TenantMateVO::getId, mate -> mate));

        // 构建新同住人ID映射
        Set<Integer> newMateIds = newMateList.stream()
            .map(TenantMateDTO::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // 1. 删除不在新列表中的同住人
        List<Integer> toDeleteIds = originalMateMap.keySet().stream()
            .filter(id -> !newMateIds.contains(id))
            .collect(Collectors.toList());

        if (!toDeleteIds.isEmpty()) {
            tenantMateRepo.removeByIds(toDeleteIds);
            // 删除关联的附件
            toDeleteIds.forEach(id -> {
                fileAttachRepo.deleteByBizIdAndBizTypes(
                    id.longValue(),
                    ListUtil.of(
                        FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_BACK.getBizType(),
                        FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_FRONT.getBizType(),
                        FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_IN_HAND.getBizType(),
                        FileAttachBizTypeEnum.TENANT_MATE_OTHER_IMAGE.getBizType()
                    )
                );
            });
        }

        // 2. 新增或更新同住人
        for (TenantMateDTO mateDTO : newMateList) {
            if (mateDTO.getId() == null) {
                // 新增
                TenantMate newMate = BeanCopyUtils.copyBean(mateDTO, TenantMate.class);
                assert newMate != null;
                newMate.setTenantId(tenantId);
                newMate.setCreateTime(DateUtil.date());
                tenantMateRepo.save(newMate);

                // 保存附件
                saveTenantMateAttachments(newMate.getId(), mateDTO);

            } else if (originalMateMap.containsKey(mateDTO.getId())) {
                // 更新
                TenantMate existingMate = tenantMateRepo.getById(mateDTO.getId());
                if (existingMate != null) {
                    BeanUtils.copyProperties(mateDTO, existingMate, "id", "tenantId", "createTime");
                    existingMate.setUpdateTime(DateUtil.date());
                    tenantMateRepo.updateById(existingMate);

                    // 更新附件（先删除后添加）
                    updateTenantMateAttachments(existingMate.getId(), mateDTO);
                }
            }
        }
    }

    /**
     * 保存同住人附件
     */
    private void saveTenantMateAttachments(Long mateId, TenantMateDTO dto) {
        if (CollUtil.isNotEmpty(dto.getIdCardBackList())) {
            fileAttachRepo.addFileAttachBatch(mateId,
                FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_BACK.getBizType(), dto.getIdCardBackList());
        }
        if (CollUtil.isNotEmpty(dto.getIdCardFrontList())) {
            fileAttachRepo.addFileAttachBatch(mateId,
                FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_FRONT.getBizType(), dto.getIdCardFrontList());
        }
        if (CollUtil.isNotEmpty(dto.getIdCardInHandList())) {
            fileAttachRepo.addFileAttachBatch(mateId,
                FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_IN_HAND.getBizType(), dto.getIdCardInHandList());
        }
        if (CollUtil.isNotEmpty(dto.getOtherImageList())) {
            fileAttachRepo.addFileAttachBatch(mateId,
                FileAttachBizTypeEnum.TENANT_MATE_OTHER_IMAGE.getBizType(), dto.getOtherImageList());
        }
    }

    /**
     * 更新同住人附件
     */
    private void updateTenantMateAttachments(Long mateId, TenantMateDTO dto) {
        // 删除旧附件
        fileAttachRepo.deleteByBizIdAndBizTypes(
            mateId,
            ListUtil.of(
                FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_BACK.getBizType(),
                FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_FRONT.getBizType(),
                FileAttachBizTypeEnum.TENANT_MATE_ID_CARD_IN_HAND.getBizType(),
                FileAttachBizTypeEnum.TENANT_MATE_OTHER_IMAGE.getBizType()
            )
        );

        // 添加新附件
        saveTenantMateAttachments(mateId, dto);
    }
}
