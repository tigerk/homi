package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.RoomDetail;
import com.homi.model.mapper.RoomDetailMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 房间扩展表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-10
 */
@Service
public class RoomDetailRepo extends ServiceImpl<RoomDetailMapper, RoomDetail> {
    /**
     * 根据 room id 查询房间扩展信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/10 18:09
     *
     * @param roomId 参数说明
     * @return com.homi.model.entity.RoomDetail
     */
    public RoomDetail getByRoomId(Long roomId) {
        LambdaQueryWrapper<RoomDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomDetail::getRoomId, roomId);
        return this.getOne(queryWrapper);
    }
}
