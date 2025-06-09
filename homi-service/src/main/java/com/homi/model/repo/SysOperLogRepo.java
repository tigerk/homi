package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.SysOperLog;
import com.homi.model.mapper.SysOperLogMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志记录表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Service
public class SysOperLogRepo extends ServiceImpl<SysOperLogMapper, SysOperLog> {

}
