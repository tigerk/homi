package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TenantCheckoutFee;
import com.homi.model.dao.mapper.TenantCheckoutFeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 退租费用明细表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-03
 */
@Service
public class TenantCheckoutFeeRepo extends ServiceImpl<TenantCheckoutFeeMapper, TenantCheckoutFee> {
    public List<TenantCheckoutFee> listByCheckoutId(Long checkoutId) {
        return lambdaQuery()
            .eq(TenantCheckoutFee::getCheckoutId, checkoutId)
            .orderByAsc(TenantCheckoutFee::getFeeType)
            .list();
    }

    public void deleteByCheckoutId(Long checkoutId) {
        lambdaUpdate()
            .eq(TenantCheckoutFee::getCheckoutId, checkoutId)
            .remove();
    }
}
