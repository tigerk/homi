package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 房源表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {

    /**
     * 获取某个集中式项目下所有房源的剩余房间数量总和
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/26 01:21
     *
     * @param leaseModeId 集中式项目ID
     * @param leaseMode   租赁模式
     * @return java.lang.Integer 剩余房间数量总和
     */
    @Select("SELECT COALESCE(SUM(room_count - rest_room_count), 0) " +
        "FROM house " +
        "WHERE lease_mode_id = #{leaseModeId} AND lease_mode = #{leaseMode}")
    Integer getTotalRentedRoomCount(@Param("leaseModeId") Long leaseModeId, @Param("leaseMode") Integer leaseMode);
}
