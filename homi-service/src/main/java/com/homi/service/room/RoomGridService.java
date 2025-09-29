package com.homi.service.room;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.domain.dto.room.*;
import com.homi.domain.dto.room.grid.FloorStatisticsDTO;
import com.homi.domain.dto.room.grid.RoomAggregatedDTO;
import com.homi.domain.dto.room.grid.RoomGridDTO;
import com.homi.model.mapper.HouseMapper;
import com.homi.model.repo.CommunityRepo;
import com.homi.model.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/9/26
 */

@Service
@RequiredArgsConstructor
public class RoomGridService {
    private final RoomRepo roomRepo;
    private final HouseMapper houseMapper;
    private final CommunityRepo communityRepo;

    /**
     * 查询小区的聚合房间数据，根据小区、楼栋、单元、楼层来聚合显示数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/29 14:27
     *
     * @param query 参数说明
     * @return java.util.List<com.homi.domain.dto.room.grid.RoomAggregatedDTO>
     */
    public List<RoomAggregatedDTO> getAggregatedRooms(RoomQueryDTO query) {
        return roomRepo.selectAggregatedRooms(query);
    }

    /**
     * 获取聚合的房间数据
     */
    @Transactional(readOnly = true)
    public RoomGridDTO getRoomGrid(RoomQueryDTO query) {
        // 一次只显示三个楼层的数据
        List<RoomAggregatedDTO> aggregatedRooms = getAggregatedRooms(query);

        // 4. 获取当前页的小区、楼栋单元和楼层信息
        long offset = (query.getCurrentPage() - 1) * query.getPageSize();
        List<RoomAggregatedDTO> currentQueryStatistic = aggregatedRooms.stream().skip(offset).limit(query.getPageSize()).toList();

        // 5. 查询这些楼层的所有房间
        IPage<RoomItemDTO> roomItemDTOIPage = roomRepo.pageRoomGridList(currentQueryStatistic, query);
        List<RoomItemDTO> rooms = roomItemDTOIPage.getRecords();

        // 7. 构建楼层分组数据


        // 8. 构建返回结果
        RoomGridDTO result = new RoomGridDTO();
        result.setCommunityGroup(propertyInfo);
        result.setFloors(floors);
        result.setTotalFloors(totalFloors);
        result.setTotalRooms(floorStats.stream()
                .mapToInt(FloorStatisticsDTO::getTotalRooms)
                .sum());
        result.setCurrentPage(query.getCurrentPage());
        result.setPageSize(query.getPageSize());
        result.setHasMore(offset + query.getFloorsPerPage() < totalFloors);

        // 计算总出租率
        int totalLeased = floorStats.stream()
                .mapToInt(FloorStatisticsDTO::getLeasedRooms)
                .sum();
        result.setOccupancyRate(
                BigDecimal.valueOf(totalLeased * 100.0 / result.getTotalRooms())
                        .setScale(2, RoundingMode.HALF_UP)
        );

        return result;
    }
}
