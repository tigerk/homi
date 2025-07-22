package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.SysLoginLog;
import com.homi.model.mapper.SysLoginLogMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统访问记录 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class SysLoginLogRepo extends ServiceImpl<SysLoginLogMapper, SysLoginLog> {

}
