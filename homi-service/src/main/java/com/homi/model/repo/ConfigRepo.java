package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.Config;
import com.homi.model.mapper.ConfigMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 参数配置表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class ConfigRepo extends ServiceImpl<ConfigMapper, Config> {

}
