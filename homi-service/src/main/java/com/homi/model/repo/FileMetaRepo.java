package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.model.entity.FileMeta;
import com.homi.model.mapper.FileMetaMapper;
import com.homi.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 临时文件资源表（防孤儿文件） 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-10-19
 */
@Service
@Slf4j
public class FileMetaRepo extends ServiceImpl<FileMetaMapper, FileMeta> {

    public FileMeta searchFileByHash(String hash) {
        LambdaQueryWrapper<FileMeta> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileMeta::getFileHash, hash);

        return getBaseMapper().selectOne(queryWrapper);
    }

    /**
     * 根据文件名设置文件为已使用
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/20 10:35
     *
     * @param fileUrlList 参数说明
     * @return java.lang.Boolean
     */
    public void setFileUsedByName(List<String> fileUrlList) {
        List<String> fileNames = fileUrlList.stream().map(ImageUtils::getFileName).toList();

        LambdaQueryWrapper<FileMeta> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(FileMeta::getFileName, fileNames);

        FileMeta fileMeta = new FileMeta();
        fileMeta.setIsUsed(StatusEnum.ACTIVE.getValue());
        boolean updated = update(fileMeta, queryWrapper);
        if (!updated) {
            log.error("图片更新为已使用失败, fileUrlList={}", fileUrlList);
        }
    }

}
