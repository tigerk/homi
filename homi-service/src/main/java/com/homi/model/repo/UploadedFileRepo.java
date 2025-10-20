package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.model.entity.UploadedFile;
import com.homi.model.mapper.UploadedFileMapper;
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
public class UploadedFileRepo extends ServiceImpl<UploadedFileMapper, UploadedFile> {

    public UploadedFile searchFileByHash(String hash) {
        LambdaQueryWrapper<UploadedFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadedFile::getFileHash, hash);

        return getBaseMapper().selectOne(queryWrapper);
    }

    /**
     * 根据文件名设置文件为已使用
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/20 10:35
     *
     * @param fileNames 参数说明
     * @return java.lang.Boolean
     */
    public Boolean setFileUsedByName(List<String> fileNames) {
        LambdaQueryWrapper<UploadedFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UploadedFile::getFileName, fileNames);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setIsUsed(StatusEnum.ACTIVE.getValue());
        return update(uploadedFile, queryWrapper);
    }

}
