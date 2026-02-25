package com.homi.model.dao.repo;

import com.homi.model.dao.entity.RoomTrack;
import com.homi.model.dao.mapper.RoomTrackMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.room.vo.RoomTrackVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 房间跟进表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-26
 */
@Service
public class RoomTrackRepo extends ServiceImpl<RoomTrackMapper, RoomTrack> {
    /**
     * 根据房间ID查询房间跟进记录
     * 按照更新时间倒序排序
     *
     * @param roomId 房间ID
     * @return 房间跟进记录列表
     */
    public List<RoomTrackVO> getRoomTracksByRoomId(Long roomId) {
        return getBaseMapper().getRoomTracksByRoomId(roomId);
    }
}
