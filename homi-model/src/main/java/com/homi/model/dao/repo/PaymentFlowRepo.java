package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.PaymentFlow;
import com.homi.model.dao.mapper.PaymentFlowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentFlowRepo extends ServiceImpl<PaymentFlowMapper, PaymentFlow> {
    public PaymentFlow getByBiz(String bizType, Long bizId) {
        LambdaQueryWrapper<PaymentFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentFlow::getBizType, bizType);
        wrapper.eq(PaymentFlow::getBizId, bizId);
        wrapper.orderByDesc(PaymentFlow::getPayAt);
        wrapper.orderByDesc(PaymentFlow::getCreateAt);
        wrapper.last("limit 1");
        return getOne(wrapper);
    }

    public List<PaymentFlow> listByBiz(String bizType, Long bizId) {
        LambdaQueryWrapper<PaymentFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentFlow::getBizType, bizType);
        wrapper.eq(PaymentFlow::getBizId, bizId);
        wrapper.orderByDesc(PaymentFlow::getPayAt);
        wrapper.orderByDesc(PaymentFlow::getCreateAt);
        return list(wrapper);
    }

    public List<PaymentFlow> listByBizIds(String bizType, List<Long> bizIds) {
        if (bizIds == null || bizIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<PaymentFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentFlow::getBizType, bizType);
        wrapper.in(PaymentFlow::getBizId, bizIds);
        wrapper.orderByDesc(PaymentFlow::getPayAt);
        wrapper.orderByDesc(PaymentFlow::getCreateAt);
        return list(wrapper);
    }
}
