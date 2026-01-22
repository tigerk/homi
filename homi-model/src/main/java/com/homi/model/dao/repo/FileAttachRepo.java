package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.FileAttach;
import com.homi.model.dao.entity.FileMeta;
import com.homi.model.dao.mapper.FileAttachMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
        AtomicInteger i = new AtomicInteger();
        idCardBackList.forEach(idCardBack -> {
            FileAttach fileAttach = new FileAttach();
            fileAttach.setBizId(id);
            fileAttach.setBizType(bizType);
            fileAttach.setFileUrl(idCardBack);
            fileAttach.setSortOrder(i.getAndIncrement());

            FileMeta fileMeta = fileMetaRepo.getFileMetaByUrl(idCardBack);
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
            .list();
    }

    public void deleteByBizIdAndBizTypes(Long bizId, List<String> bizTypes) {
        LambdaQueryWrapper<FileAttach> wrapper = new LambdaQueryWrapper<FileAttach>()
            .eq(FileAttach::getBizId, bizId)
            .in(FileAttach::getBizType, bizTypes);

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
}
