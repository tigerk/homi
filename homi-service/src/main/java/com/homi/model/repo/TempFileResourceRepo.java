package com.homi.model.repo;

import com.homi.model.entity.TempFileResource;
import com.homi.model.mapper.TempFileResourceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class TempFileResourceRepo extends ServiceImpl<TempFileResourceMapper, TempFileResource> {

}
