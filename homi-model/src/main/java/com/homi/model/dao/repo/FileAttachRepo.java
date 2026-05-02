package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.FileMeta;
import com.homi.model.dao.mapper.FileAttachMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 通用文件资源表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-10-19
 */
@Service
@RequiredArgsConstructor
public class FileAttachRepo extends ServiceImpl<FileAttachMapper, FileAttach> {
    private final FileMetaRepo fileMetaRepo;

    /**
     * 添加文件附件批量
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:13
     *
     * @param id             参数说明
     * @param bizType        参数说明
     * @param idCardBackList 参数说明
     */
    public void addFileAttachBatch(Long id, String bizType, List<String> idCardBackList) {
        addFileAttachBatch(id, bizType, null, idCardBackList);
    }

    public void addFileAttachBatch(Long id, String bizType, String bizSubtype, List<String> fileUrlList) {
        if (fileUrlList == null || fileUrlList.isEmpty()) {
            return;
        }
        AtomicInteger i = new AtomicInteger();
        fileUrlList.forEach(fileUrl -> {
            FileAttach fileAttach = new FileAttach();
            fileAttach.setBizId(id);
            fileAttach.setBizType(bizType);
            fileAttach.setBizSubtype(bizSubtype);
            fileAttach.setFileUrl(fileUrl);
            fileAttach.setSortOrder(i.getAndIncrement());

            FileMeta fileMeta = fileMetaRepo.getFileMetaByUrl(fileUrl);
            if (fileMeta != null) {
                fileAttach.setFileType(fileMeta.getFileType());
            }

            save(fileAttach);
        });
    }

    public List<FileAttach> getFileAttachListByBizIdAndBizTypes(Long tenantId, List<String> of) {
        return lambdaQuery()
            .eq(FileAttach::getBizId, tenantId)
            .in(FileAttach::getBizType, of)
            .orderByAsc(FileAttach::getSortOrder)
            .orderByAsc(FileAttach::getCreateAt)
            .orderByAsc(FileAttach::getId)
            .list();
    }

    public List<FileAttach> getFileAttachListByBizIdAndBizTypeAndSubtype(Long bizId, String bizType, String bizSubtype) {
        return lambdaQuery()
            .eq(FileAttach::getBizId, bizId)
            .eq(FileAttach::getBizType, bizType)
            .eq(FileAttach::getBizSubtype, bizSubtype)
            .orderByAsc(FileAttach::getSortOrder)
            .orderByAsc(FileAttach::getCreateAt)
            .orderByAsc(FileAttach::getId)
            .list();
    }

    /**
     * 按业务主键和业务类型读取附件，用于业务日志快照等需要比较整组附件的场景。
     */
    public List<FileAttach> getFileAttachListByBizIdAndBizType(Long bizId, String bizType) {
        return lambdaQuery()
            .eq(FileAttach::getBizId, bizId)
            .eq(FileAttach::getBizType, bizType)
            .orderByAsc(FileAttach::getBizSubtype)
            .orderByAsc(FileAttach::getSortOrder)
            .orderByAsc(FileAttach::getCreateAt)
            .orderByAsc(FileAttach::getId)
            .list();
    }

    public void deleteByBizIdAndBizTypes(Long bizId, List<String> bizTypes) {
        LambdaQueryWrapper<FileAttach> wrapper = new LambdaQueryWrapper<FileAttach>()
            .eq(FileAttach::getBizId, bizId)
            .in(FileAttach::getBizType, bizTypes);

        remove(wrapper);
    }

    public void deleteByBizIdAndBizTypeAndSubtype(Long bizId, String bizType, String bizSubtype) {
        LambdaQueryWrapper<FileAttach> wrapper = new LambdaQueryWrapper<FileAttach>()
            .eq(FileAttach::getBizId, bizId)
            .eq(FileAttach::getBizType, bizType)
            .eq(FileAttach::getBizSubtype, bizSubtype);

        remove(wrapper);
    }

    /**
     * 原有文件全部删除掉，重新创建文件附件列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:13
     *
     * @param bizId       业务 ID
     * @param bizType     业务类型
     * @param fileUrlList 文件 URL 列表
     */
    public void recreateFileAttachList(Long bizId, String bizType, List<String> fileUrlList) {
        deleteByBizIdAndBizTypes(bizId, List.of(bizType));
        addFileAttachBatch(bizId, bizType, fileUrlList);
    }

    public void recreateFileAttachList(Long bizId, String bizType, String bizSubtype, List<String> fileUrlList) {
        deleteByBizIdAndBizTypeAndSubtype(bizId, bizType, bizSubtype);
        addFileAttachBatch(bizId, bizType, bizSubtype, fileUrlList);
    }

    public void recreateFileAttachListBySubtypeGroups(Long bizId, String bizType, Map<String, List<String>> subtypeFileUrlMap) {
        deleteByBizIdAndBizTypes(bizId, List.of(bizType));
        subtypeFileUrlMap.forEach((bizSubtype, fileUrlList) -> addFileAttachBatch(bizId, bizType, bizSubtype, fileUrlList));
    }
}
