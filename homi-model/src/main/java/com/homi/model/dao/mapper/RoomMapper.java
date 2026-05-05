package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.model.community.dto.CommunityDTO;
import com.homi.model.dao.entity.Room;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.model.room.vo.RoomOccupancyStatusTotalVO;
import com.homi.model.room.vo.grid.RoomAggregatedVO;
import com.homi.model.dashboard.vo.WelcomeCountBucketVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.homi.domain.vo.room.RoomListVO>
     */
    IPage<RoomListVO> pageRoomList(IPage<RoomListVO> page, @Param("query") RoomQueryDTO query);

    List<RoomOccupancyStatusTotalVO> getStatusTotal(@Param("query") RoomQueryDTO query);

    List<CommunityDTO> selectRoomCommunityOptions(@Param("query") RoomQueryDTO query);

    /**
     * 查询小区的聚合数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/29 14:21
     *
     * @param query 参数说明
     * @return java.util.List<com.homi.domain.vo.room.grid.RoomAggregatedVO>
     */
    List<RoomAggregatedVO> selectAggregatedRooms(@Param("query") RoomQueryDTO query);

    /**
     * 查询锁定的房间数量
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/29 14:21
     *
     * @param query 参数说明
     * @return java.lang.Long
     */
    Integer countByLocked(@Param("query") RoomQueryDTO query);

    Integer countByClosed(@Param("query") RoomQueryDTO query);

    List<WelcomeCountBucketVO> selectWelcomeVacancyBuckets(@Param("leaseMode") Integer leaseMode);

    Room selectRoomByIdIncludeDeleted(@Param("roomId") Long roomId);

    Integer countActiveByHouseId(@Param("houseId") Long houseId);

    Integer countActiveSameRoomNumber(@Param("houseId") Long houseId, @Param("roomNumber") String roomNumber, @Param("excludeRoomId") Long excludeRoomId);

    Integer countActiveOwnerContractByRoom(@Param("houseId") Long houseId,
                                           @Param("leaseMode") Integer leaseMode,
                                           @Param("leaseModeId") Long leaseModeId,
                                           @Param("building") String building,
                                           @Param("unit") String unit,
                                           @Param("statuses") List<Integer> statuses);

    Integer markDeleted(@Param("roomId") Long roomId,
                        @Param("deleteReason") String deleteReason,
                        @Param("deleteBy") Long deleteBy,
                        @Param("deleteAt") Date deleteAt);

    Integer markRestored(@Param("roomId") Long roomId,
                         @Param("restoreReason") String restoreReason,
                         @Param("restoreBy") Long restoreBy,
                         @Param("restoreAt") Date restoreAt);
}
