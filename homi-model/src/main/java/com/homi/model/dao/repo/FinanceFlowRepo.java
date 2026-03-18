package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.FinanceFlow;
import com.homi.model.dao.mapper.FinanceFlowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceFlowRepo extends ServiceImpl<FinanceFlowMapper, FinanceFlow> {
    public List<FinanceFlow> getListByBiz(String bizType, Long bizId) {
        LambdaQueryWrapper<FinanceFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceFlow::getBizType, bizType);
        wrapper.eq(FinanceFlow::getBizId, bizId);
        wrapper.orderByDesc(FinanceFlow::getFlowTime);
        wrapper.orderByDesc(FinanceFlow::getCreateTime);
        return list(wrapper);
    }

    public List<FinanceFlow> getListByBizIds(String bizType, List<Long> bizIds) {
        if (bizIds == null || bizIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<FinanceFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceFlow::getBizType, bizType);
        wrapper.in(FinanceFlow::getBizId, bizIds);
        wrapper.orderByDesc(FinanceFlow::getFlowTime);
        wrapper.orderByDesc(FinanceFlow::getCreateTime);
        return list(wrapper);
    }

    public boolean existsByBizIds(String bizType, List<Long> bizIds) {
        if (bizIds == null || bizIds.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<FinanceFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceFlow::getBizType, bizType);
        wrapper.in(FinanceFlow::getBizId, bizIds);
        wrapper.last("limit 1");
        return getOne(wrapper) != null;
    }
}
