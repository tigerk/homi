package com.homi.model.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.monitor.OperationLogDTO;
import com.homi.event.OperationLogEvent;
import com.homi.model.entity.OperationLog;
import com.homi.model.mapper.OperationLogMapper;
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
public class OperationLogRepo extends ServiceImpl<OperationLogMapper, OperationLog> {
    @Resource
    private UserRepo userRepo;

    @Resource
    private UserCompanyRepo userCompanyRepo;

    /**
     * 操作日志记录
     *
     * @param operationLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void addOperationLog(OperationLogEvent operationLogEvent) {
        OperationLog operationLog = BeanCopyUtils.copyBean(operationLogEvent, OperationLog.class);
        assert operationLog != null;
        // 远程查询操作地点
        operationLog.setLocation(AddressUtils.getRealAddressByIP(operationLog.getIpAddress()));

        getBaseMapper().insert(operationLog);
    }

    public PageVO<OperationLog> getList(OperationLogDTO dto) {
        Page<OperationLog> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());

        LambdaQueryWrapper<OperationLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(dto.getTitle())) {
            lambdaQueryWrapper.like(OperationLog::getTitle, dto.getTitle());
        }
        if (Objects.nonNull(dto.getStatus())) {
            lambdaQueryWrapper.eq(OperationLog::getStatus, dto.getStatus());
        }

        if (Objects.nonNull(dto.getRequestTime()) && dto.getRequestTime().size() == 2) {
            lambdaQueryWrapper.ge(OperationLog::getRequestTime, dto.getRequestTime().get(0));
            lambdaQueryWrapper.le(OperationLog::getRequestTime, dto.getRequestTime().get(1));
        }
        lambdaQueryWrapper.orderByDesc(OperationLog::getRequestTime);

        IPage<OperationLog> sysOperationLogPage = getBaseMapper().selectPage(page, lambdaQueryWrapper);

        PageVO<OperationLog> pageVO = new PageVO<>();
        pageVO.setTotal(sysOperationLogPage.getTotal());
        pageVO.setList(sysOperationLogPage.getRecords());
        pageVO.setCurrentPage(sysOperationLogPage.getCurrent());
        pageVO.setPageSize(sysOperationLogPage.getSize());
        pageVO.setPages(sysOperationLogPage.getPages());

        return pageVO;
    }

    public OperationLog getDetailById(Long id) {
        return getBaseMapper().selectById(id);
    }

    public int clearAllByCompanyId(Long companyId) {
        return getBaseMapper().delete(new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getCompanyId, companyId));
    }

    public int batchDeleteByIds(List<Long> ids) {
        return getBaseMapper().deleteBatchIds(ids);
    }
}
