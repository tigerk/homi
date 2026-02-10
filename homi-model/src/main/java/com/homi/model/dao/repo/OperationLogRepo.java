package com.homi.model.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.event.OperationLogEvent;
import com.homi.common.lib.utils.AddressUtils;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.OperationLog;
import com.homi.model.dao.mapper.OperationLogMapper;
import com.homi.model.monitor.OperationLogDTO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
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
    private CompanyUserRepo companyUserRepo;

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
        if (CharSequenceUtil.isNotBlank(dto.getUsername())) {
            lambdaQueryWrapper.like(OperationLog::getUsername, dto.getUsername());
        }
        if (Objects.nonNull(dto.getStatus())) {
            lambdaQueryWrapper.eq(OperationLog::getStatus, dto.getStatus());
        }

        if (Objects.nonNull(dto.getRequestTime()) && dto.getRequestTime().size() == 2) {
            lambdaQueryWrapper.ge(OperationLog::getRequestTime, dto.getRequestTime().get(0));
            lambdaQueryWrapper.le(OperationLog::getRequestTime, dto.getRequestTime().get(1));
        }
        lambdaQueryWrapper.orderByDesc(OperationLog::getRequestTime);

        return getOperationLogPageVO(page, lambdaQueryWrapper);
    }

    public OperationLog getDetailById(Long id) {
        return getBaseMapper().selectById(id);
    }

    public PageVO<OperationLog> getMineOperationLogs(Long companyId, Long userId, List<String> titles, long currentPage, long pageSize) {
        Page<OperationLog> page = new Page<>(currentPage, pageSize);

        LambdaQueryWrapper<OperationLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OperationLog::getCompanyId, companyId)
            .eq(OperationLog::getUserId, userId)
            .in(OperationLog::getTitle, titles)
            .orderByDesc(OperationLog::getRequestTime);

        return getOperationLogPageVO(page, lambdaQueryWrapper);
    }

    @NotNull
    private PageVO<OperationLog> getOperationLogPageVO(Page<OperationLog> page, LambdaQueryWrapper<OperationLog> lambdaQueryWrapper) {
        IPage<OperationLog> operationLogPage = getBaseMapper().selectPage(page, lambdaQueryWrapper);

        PageVO<OperationLog> pageVO = new PageVO<>();
        pageVO.setTotal(operationLogPage.getTotal());
        pageVO.setList(operationLogPage.getRecords());
        pageVO.setCurrentPage(operationLogPage.getCurrent());
        pageVO.setPageSize(operationLogPage.getSize());
        pageVO.setPages(operationLogPage.getPages());

        return pageVO;
    }

    public int clearAllByCompanyId(Long companyId) {
        return getBaseMapper().delete(new LambdaQueryWrapper<OperationLog>().eq(OperationLog::getCompanyId, companyId));
    }

    public boolean batchDeleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}
