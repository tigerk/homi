package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.dto.room.RoomTotalItemDTO;
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

     * @param page 参数说明
     * @param query 参数说明
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.homi.domain.dto.room.RoomItemDTO>
     */
    IPage<RoomItemDTO> getPage(IPage<RoomItemDTO> page, @Param("query") RoomQueryDTO query);

    List<RoomTotalItemDTO> getStatusTotal(@Param("query") RoomQueryDTO query);
}
