package com.homi.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.dao.entity.RoomPricePlan;
import com.homi.dao.mapper.RoomPricePlanMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 房间租金方案表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-10-28
 */
@Service
public class RoomPricePlanRepo extends ServiceImpl<RoomPricePlanMapper, RoomPricePlan> {
    public List<RoomPricePlan> listByRoomId(Long id) {
        return baseMapper.selectList(new LambdaQueryWrapper<RoomPricePlan>().eq(RoomPricePlan::getRoomId, id));
    }
}
