package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TenantCheckoutFee;
import com.homi.model.dao.mapper.TenantCheckoutFeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 退租费用明细表 服务实现类
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class TenantCheckoutFeeRepo extends ServiceImpl<TenantCheckoutFeeMapper, TenantCheckoutFee> {

    /**
     * 根据退租单ID查询费用列表
     */
    public List<TenantCheckoutFee> listByCheckoutId(Long checkoutId) {
        return lambdaQuery()
            .eq(TenantCheckoutFee::getCheckoutId, checkoutId)
            .orderByAsc(TenantCheckoutFee::getFeeDirection)
            .orderByAsc(TenantCheckoutFee::getFeeType)
            .list();
    }

    /**
     * 根据退租单ID删除费用明细
     */
    public void deleteByCheckoutId(Long checkoutId) {
        lambdaUpdate()
            .eq(TenantCheckoutFee::getCheckoutId, checkoutId)
            .remove();
    }
}
