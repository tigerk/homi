package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.mapper.TenantContractMapper;
import com.homi.model.vo.contract.TenantContractVO;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 租赁合同信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Service
public class TenantContractRepo extends ServiceImpl<TenantContractMapper, TenantContract> {
    public TenantContractVO getTenantContractByTenantId(Long id) {
        LambdaQueryWrapper<TenantContract> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantContract::getTenantId, id);

        TenantContract tenantContract = getOne(queryWrapper);

        return BeanCopyUtils.copyBean(tenantContract, TenantContractVO.class);
    }
}
