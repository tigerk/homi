package com.homi.model.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.monitor.OperationLogDTO;
import com.homi.event.OperationLogEvent;
import com.homi.model.entity.SysOperationLog;
import com.homi.model.entity.User;
import com.homi.model.mapper.SysOperationLogMapper;
import com.homi.utils.AddressUtils;
import com.homi.utils.BeanCopyUtils;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    @Resource
    private UserRepo userRepo;

    @Resource
    private CompanyUserRepo companyUserRepo;

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

    public PageVO<SysOperationLog> getList(OperationLogDTO dto) {
        Page<SysOperationLog> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());

        LambdaQueryWrapper<SysOperationLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(dto.getTitle())) {
            lambdaQueryWrapper.like(SysOperationLog::getTitle, dto.getTitle());
        }
        if (Objects.nonNull(dto.getStatus())) {
            lambdaQueryWrapper.eq(SysOperationLog::getStatus, dto.getStatus());
        }

        if (Objects.nonNull(dto.getRequestTime()) && dto.getRequestTime().size() == 2) {
            lambdaQueryWrapper.ge(SysOperationLog::getRequestTime, dto.getRequestTime().get(0));
            lambdaQueryWrapper.le(SysOperationLog::getRequestTime, dto.getRequestTime().get(1));
        }
        lambdaQueryWrapper.orderByDesc(SysOperationLog::getRequestTime);

        IPage<SysOperationLog> sysOperationLogPage = getBaseMapper().selectPage(page, lambdaQueryWrapper);

        PageVO<SysOperationLog> pageVO = new PageVO<>();
        pageVO.setTotal(sysOperationLogPage.getTotal());
        pageVO.setList(sysOperationLogPage.getRecords());
        pageVO.setCurrentPage(sysOperationLogPage.getCurrent());
        pageVO.setPageSize(sysOperationLogPage.getSize());
        pageVO.setPages(sysOperationLogPage.getPages());

        return pageVO;
    }

    public SysOperationLog getDetailById(Long id) {
        return getBaseMapper().selectById(id);
    }

    public int clearAllByCompanyId(Long companyId) {
        return getBaseMapper().delete(new LambdaQueryWrapper<SysOperationLog>().eq(SysOperationLog::getCompanyId, companyId));
    }

    public int batchDeleteByIds(List<Long> ids) {
        return getBaseMapper().deleteBatchIds(ids);
    }
}
