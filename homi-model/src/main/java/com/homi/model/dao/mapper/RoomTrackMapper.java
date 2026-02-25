package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.RoomTrack;
import com.homi.model.room.vo.RoomTrackVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 房间跟进表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2026-02-26
 */
@Mapper
public interface RoomTrackMapper extends BaseMapper<RoomTrack> {

    /**
     * 根据房间ID查询房间跟进记录
     * 按照更新时间倒序排序
     *
     * @param roomId 房间ID
     * @return 房间跟进记录列表
     */
    List<RoomTrackVO> getRoomTracksByRoomId(Long roomId);
}
