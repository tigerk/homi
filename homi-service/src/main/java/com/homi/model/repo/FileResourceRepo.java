package com.homi.model.repo;

import com.homi.model.entity.FileResource;
import com.homi.model.mapper.FileResourceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用文件资源表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-10-19
 */
@Service
public class FileResourceRepo extends ServiceImpl<FileResourceMapper, FileResource> {

}
