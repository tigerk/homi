package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.LeaseCheckoutFee;
import com.homi.model.dao.mapper.LeaseCheckoutFeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 退租费用明细表 服务实现类
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class LeaseCheckoutFeeRepo extends ServiceImpl<LeaseCheckoutFeeMapper, LeaseCheckoutFee> {

    /**
     * 根据退租单ID查询费用列表
     */
    public List<LeaseCheckoutFee> listByCheckoutId(Long checkoutId) {
        return lambdaQuery()
            .eq(LeaseCheckoutFee::getCheckoutId, checkoutId)
            .orderByAsc(LeaseCheckoutFee::getFeeDirection)
            .orderByAsc(LeaseCheckoutFee::getFeeType)
            .list();
    }

    /**
     * 根据退租单ID删除费用明细
     */
    public void deleteByCheckoutId(Long checkoutId) {
        lambdaUpdate()
            .eq(LeaseCheckoutFee::getCheckoutId, checkoutId)
            .remove();
    }
}
