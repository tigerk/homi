package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.UploadedFile;
import com.homi.model.mapper.UploadedFileMapper;
import org.springframework.stereotype.Service;

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
}
