package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.mapper.TenantContractMapper;
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
}
