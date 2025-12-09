package com.homi.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.monitor.LoginLogDTO;
import com.homi.event.LoginLogEvent;
import com.homi.dao.entity.LoginLog;
import com.homi.dao.mapper.LoginLogMapper;
import com.homi.utils.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 系统访问记录 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class LoginLogRepo extends ServiceImpl<LoginLogMapper, LoginLog> {
    @Resource
    private UserRepo userRepo;

    @Resource
    private CompanyUserRepo companyUserRepo;

    /**
     * 登录日志记录
     */
    @Async
    @EventListener
    public void recordLogin(LoginLogEvent loginLogEvent) {
        LoginLog loginLog = BeanCopyUtils.copyBean(loginLogEvent, LoginLog.class);

        getBaseMapper().insert(loginLog);
    }

    /**
     * 获取当前用户的登录日志
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/23 11:37
     *
     * @param dto 查询条件
     * @return java.util.List<com.homi.model.entity.LoginLog>
     */
    public PageVO<LoginLog> getLoginLogList(LoginLogDTO dto) {
        Page<LoginLog> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());

        LambdaQueryWrapper<LoginLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(dto.getUsername())) {
            lambdaQueryWrapper.like(LoginLog::getUsername, dto.getUsername());
        }
        if (Objects.nonNull(dto.getStatus())) {
            lambdaQueryWrapper.eq(LoginLog::getStatus, dto.getStatus());
        }

        if (Objects.nonNull(dto.getLoginTime()) && dto.getLoginTime().size() == 2) {
            lambdaQueryWrapper.ge(LoginLog::getLoginTime, dto.getLoginTime().get(0));
            lambdaQueryWrapper.le(LoginLog::getLoginTime, dto.getLoginTime().get(1));
        }
        lambdaQueryWrapper.orderByDesc(LoginLog::getLoginTime);

        IPage<LoginLog> sysLoginLogIPage = getBaseMapper().selectPage(page, lambdaQueryWrapper);

        PageVO<LoginLog> pageVO = new PageVO<>();
        pageVO.setTotal(sysLoginLogIPage.getTotal());
        pageVO.setList(sysLoginLogIPage.getRecords());
        pageVO.setCurrentPage(sysLoginLogIPage.getCurrent());
        pageVO.setPageSize(sysLoginLogIPage.getSize());
        pageVO.setPages(sysLoginLogIPage.getPages());

        return pageVO;
    }

    /**
     * 获取当前用户的登录日志
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/23 11:37
     *
     * @param companyId   参数说明
     * @param loginTokens 参数说明
     * @return java.util.List<com.homi.model.entity.LoginLog>
     */
    public List<LoginLog> getLoginUsers(Long companyId, List<String> loginTokens) {
        return getBaseMapper().selectList(new LambdaQueryWrapper<LoginLog>()
            .eq(LoginLog::getCompanyId, companyId)
            .in(LoginLog::getLoginToken, loginTokens));
    }

    public int clearAllByCompanyId(Long companyId) {
        return getBaseMapper().delete(new LambdaQueryWrapper<LoginLog>()
            .eq(LoginLog::getCompanyId, companyId));
    }

    public int batchDeleteByIds(List<Long> ids) {
        return getBaseMapper().delete(new LambdaQueryWrapper<LoginLog>()
            .in(LoginLog::getId, ids));
    }
}
