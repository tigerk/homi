package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TenantContractRoom;
import com.homi.model.dao.mapper.TenantContractRoomMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 合同房间表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-19
 */
@Service
public class TenantContractRoomRepo extends ServiceImpl<TenantContractRoomMapper, TenantContractRoom> {
}
