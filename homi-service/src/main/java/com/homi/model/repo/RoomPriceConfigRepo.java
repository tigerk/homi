package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.RoomPriceConfig;
import com.homi.model.mapper.RoomPriceConfigMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 房间价格表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-10-28
 */
@Service
public class RoomPriceConfigRepo extends ServiceImpl<RoomPriceConfigMapper, RoomPriceConfig> {

    public RoomPriceConfig getByRoomId(Long id) {
        return baseMapper.selectOne(new LambdaQueryWrapper<RoomPriceConfig>().eq(RoomPriceConfig::getRoomId, id));
    }
}
