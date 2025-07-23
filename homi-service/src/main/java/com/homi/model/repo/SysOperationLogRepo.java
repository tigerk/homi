package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.event.OperationLogEvent;
import com.homi.model.entity.SysOperationLog;
import com.homi.model.mapper.SysOperationLogMapper;
import com.homi.utils.AddressUtils;
import com.homi.utils.BeanCopyUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志记录表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
public class SysOperationLogRepo extends ServiceImpl<SysOperationLogMapper, SysOperationLog> {

    /**
     * 操作日志记录
     *
     * @param operationLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void addOperationLog(OperationLogEvent operationLogEvent) {
        SysOperationLog sysOperationLog = BeanCopyUtils.copyBean(operationLogEvent, SysOperationLog.class);
        // 远程查询操作地点
        sysOperationLog.setLocation(AddressUtils.getRealAddressByIP(sysOperationLog.getIpAddress()));
        getBaseMapper().insert(sysOperationLog);
    }
}
