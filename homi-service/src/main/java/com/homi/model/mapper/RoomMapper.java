package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.dto.room.RoomTotalItemDTO;
import com.homi.domain.dto.room.grid.RoomAggregatedDTO;
import com.homi.model.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 房间表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 查询房间列表数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/20 23:55
     *
     * @param page  参数说明
     * @param query 参数说明
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.homi.domain.dto.room.RoomItemDTO>
     */
    IPage<RoomItemDTO> pageRoomList(IPage<RoomItemDTO> page, @Param("query") RoomQueryDTO query);

    List<RoomTotalItemDTO> getStatusTotal(@Param("query") RoomQueryDTO query);

    /**
     * 查询小区的聚合数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/29 14:21
     *
     * @param query 参数说明
     * @return java.util.List<com.homi.domain.dto.room.grid.RoomAggregatedDTO>
     */
    List<RoomAggregatedDTO> selectAggregatedRooms(@Param("query") RoomQueryDTO query);
}
