package com.homi.service.room;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.dto.room.RoomTotalItemDTO;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.domain.enums.house.LeaseModeEnum;
import com.homi.domain.vo.room.RoomGridVO;
import com.homi.model.entity.Focus;
import com.homi.model.entity.House;
import com.homi.model.entity.Room;
import com.homi.model.repo.FocusRepo;
import com.homi.model.repo.HouseRepo;
import com.homi.model.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepo roomRepo;

    private final HouseRepo houseRepo;
    private final FocusRepo focusRepo;

    /**
     * 获取房间列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @param query 参数说明
     * @return com.homi.domain.base.ResponseResult<com.homi.domain.dto.room.RoomListVO>
     */
    public PageVO<RoomItemDTO> getRoomList(RoomQueryDTO query) {
        Page<RoomItemDTO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<RoomItemDTO> roomPage = roomRepo.getBaseMapper().getPage(page, query);

        roomPage.getRecords().forEach(this::format);

        // 封装返回结果
        PageVO<RoomItemDTO> pageVO = new PageVO<>();
        pageVO.setTotal(roomPage.getTotal());
        pageVO.setList(roomPage.getRecords());
        pageVO.setCurrentPage(roomPage.getCurrent());
        pageVO.setPageSize(roomPage.getSize());
        pageVO.setPages(roomPage.getPages());

        return pageVO;
    }

    public void format(RoomItemDTO room) {
        if(room.getLeaseMode().equals(LeaseModeEnum.FOCUS.getCode())) {
            Focus byId = focusRepo.getById(room.getModeRefId());
            room.setPropertyName(byId.getFocusName());
        }

        RoomStatusEnum roomStatusEnum = EnumUtil.getBy(RoomStatusEnum::getCode, room.getRoomStatus());
        room.setRoomStatusName(roomStatusEnum.getName());
        room.setRoomStatusColor(roomStatusEnum.getColor());
    }

    public List<RoomTotalItemDTO> getRoomStatusTotal(RoomQueryDTO query) {
        Map<Integer, RoomTotalItemDTO> result = new HashMap<>();
        RoomStatusEnum[] values = RoomStatusEnum.values();
        for (RoomStatusEnum roomStatusEnum : values) {
            RoomTotalItemDTO roomTotalItemDTO = new RoomTotalItemDTO();
            roomTotalItemDTO.setRoomStatus(roomStatusEnum.getCode());
            roomTotalItemDTO.setRoomStatusName(roomStatusEnum.getName());
            roomTotalItemDTO.setRoomStatusColor(roomStatusEnum.getColor());
            roomTotalItemDTO.setTotal(0);
            result.put(roomStatusEnum.getCode(), roomTotalItemDTO);
        }

        query.setRoomStatus(null);

        List<RoomTotalItemDTO> statusTotal = roomRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(roomTotalItemDTO -> {
            RoomTotalItemDTO orDefault = result.getOrDefault(roomTotalItemDTO.getRoomStatus(), roomTotalItemDTO);
            orDefault.setTotal(roomTotalItemDTO.getTotal());
        });

        return result.values().stream().toList();
    }

    /**
     * 获取房间网格视图数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/24 01:50

      * @param query 参数说明
     * @return java.util.List<com.homi.domain.vo.room.RoomGridVO>
     */
    public List<RoomGridVO> getRoomGrid(RoomQueryDTO query) {
        // 设置分页参数以获取所有房间数据
        query.setCurrentPage(1L);
        query.setPageSize(10000L);
        PageVO<RoomItemDTO> roomPage = getRoomList(query);

        Map<String, House> allHouseMap = houseRepo.list().stream()
            .collect(Collectors.toMap(House::getHouseCode, house -> house));

        List<RoomItemDTO> allRooms = roomPage.getList();

        // 按houseId分组
        Map<String, List<RoomItemDTO>> roomsGroupedByHouse = allRooms.stream()
            .collect(Collectors.groupingBy(RoomItemDTO::getHouseCode));

        // 处理每个house的数据
        return roomsGroupedByHouse.entrySet().stream()
            .map(entry -> {
                String houseCode = entry.getKey();

                RoomGridVO roomGridVO = new RoomGridVO();
                roomGridVO.setHouseCode(entry.getKey());
                roomGridVO.setHouseId(allHouseMap.get(houseCode).getId());
                roomGridVO.setHouseName(allHouseMap.get(houseCode).getHouseName());
                roomGridVO.setTotal((long) entry.getValue().size());

                List<RoomItemDTO> roomsInHouse = entry.getValue();
                // 按floor分组
                Map<Integer, List<RoomItemDTO>> roomsGroupedByFloor = roomsInHouse.stream()
                    .collect(Collectors.groupingBy(RoomItemDTO::getFloor));

                // 创建楼层列表
                List<RoomGridVO.HouseFloorGridDTO> floorGridList = roomsGroupedByFloor.entrySet().stream()
                    .map(floorEntry -> {
                        Integer floor = floorEntry.getKey();
                        List<RoomItemDTO> roomsOnFloor = floorEntry.getValue();

                        RoomGridVO.HouseFloorGridDTO floorDTO = new RoomGridVO.HouseFloorGridDTO();
                        floorDTO.setFloor(floor);
                        floorDTO.setRoomList(roomsOnFloor);
                        floorDTO.setTotal((long) roomsOnFloor.size());

                        Pair<Long, BigDecimal> longBigDecimalPair = calculateLeasedRateAndCount(roomsOnFloor);
                        floorDTO.setLeasedRate(longBigDecimalPair.getValue());

                        return floorDTO;
                    }).toList();

                roomGridVO.setFloorList(floorGridList);

                // 计算整体出租率
                Pair<Long, BigDecimal> longBigDecimalPair = calculateLeasedRateAndCount(roomsInHouse);
                roomGridVO.setLeasedRate(longBigDecimalPair.getValue());

                return roomGridVO;
            })
            .collect(Collectors.toList());
    }

    /**
     * 计算房间出租率和数量
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/24 01:30
     *
     * @param roomsList 参数说明
     * @return cn.hutool.core.lang.Pair<java.lang.Long,java.math.BigDecimal>
     */
    private Pair<Long, BigDecimal> calculateLeasedRateAndCount(List<RoomItemDTO> roomsList) {
        // 计算出租率
        long leasedCount = roomsList.stream()
            .map(RoomItemDTO::getRoomStatus)
            .filter(status -> status != null && status.equals(RoomStatusEnum.LEASED.getCode()))
            .count();

        BigDecimal leasedRate = BigDecimal.ZERO;
        if (!roomsList.isEmpty()) {
            leasedRate = BigDecimal.valueOf(leasedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(roomsList.size()), 2, RoundingMode.HALF_UP);
        }

        return Pair.of(leasedCount, leasedRate);
    }
}
