package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.event.LoginLogEvent;
import com.homi.model.entity.SysLoginLog;
import com.homi.model.entity.User;
import com.homi.model.mapper.SysLoginLogMapper;
import com.homi.utils.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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
    @Resource
    private UserRepo userRepo;

    /**
     * 登录日志记录
     */
    @Async
    @EventListener
    public void recordLogin(LoginLogEvent loginLogEvent) {
        SysLoginLog loginLog = BeanCopyUtils.copyBean(loginLogEvent, SysLoginLog.class);

        User user = userRepo.getUserByUsername(loginLog.getUsername());
        if (user != null) {
            loginLog.setCompanyId(user.getCompanyId());
        }

        getBaseMapper().insert(loginLog);
    }

}
