package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.util.List;

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

    /**
     * 获取当前用户的登录日志
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/23 11:37
     *
     * @param companyId  参数说明
     * @param sessionIds 参数说明
     * @return java.util.List<com.homi.model.entity.SysLoginLog>
     */
    public List<SysLoginLog> getLoginUsers(Long companyId, List<String> sessionIds) {
        return getBaseMapper().selectList(new LambdaQueryWrapper<SysLoginLog>()
                .eq(SysLoginLog::getCompanyId, companyId)
                .in(SysLoginLog::getSessionId, sessionIds));
    }
}
