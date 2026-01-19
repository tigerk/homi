package com.homi.model.dao.repo;

import com.homi.model.dao.entity.Delivery;
import com.homi.model.dao.mapper.DeliveryMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用物业交割主表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-01-19
 */
@Service
public class DeliveryRepo extends ServiceImpl<DeliveryMapper, Delivery> {

}
