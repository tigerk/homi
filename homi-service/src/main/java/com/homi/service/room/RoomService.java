package com.homi.service.room;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.enums.room.RoomStatusEnum;
import com.homi.domain.enums.house.LeaseModeEnum;
import com.homi.domain.vo.room.RoomGridVO;
import com.homi.domain.vo.room.RoomItemVO;
import com.homi.domain.vo.room.RoomTotalItemVO;
import com.homi.model.entity.Focus;
import com.homi.model.entity.House;
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
    public PageVO<RoomItemVO> getRoomList(RoomQueryDTO query) {
        Page<RoomItemVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<RoomItemVO> roomPage = roomRepo.getBaseMapper().pageRoomList(page, query);

        roomPage.getRecords().forEach(this::format);

        // 封装返回结果
        PageVO<RoomItemVO> pageVO = new PageVO<>();
        pageVO.setTotal(roomPage.getTotal());
        pageVO.setList(roomPage.getRecords());
        pageVO.setCurrentPage(roomPage.getCurrent());
        pageVO.setPageSize(roomPage.getSize());
        pageVO.setPages(roomPage.getPages());

        return pageVO;
    }

    public void format(RoomItemVO room) {
        if(room.getLeaseMode().equals(LeaseModeEnum.FOCUS.getCode())) {
            Focus byId = focusRepo.getById(room.getModeRefId());
            room.setCommunityName(byId.getFocusName());
        }

        RoomStatusEnum roomStatusEnum = EnumUtil.getBy(RoomStatusEnum::getCode, room.getRoomStatus());
        room.setRoomStatusName(roomStatusEnum.getName());
        room.setRoomStatusColor(roomStatusEnum.getColor());
    }

    public List<RoomTotalItemVO> getRoomStatusTotal(RoomQueryDTO query) {
        Map<Integer, RoomTotalItemVO> result = new HashMap<>();
        RoomStatusEnum[] values = RoomStatusEnum.values();
        for (RoomStatusEnum roomStatusEnum : values) {
            RoomTotalItemVO roomTotalItemVO = new RoomTotalItemVO();
            roomTotalItemVO.setRoomStatus(roomStatusEnum.getCode());
            roomTotalItemVO.setRoomStatusName(roomStatusEnum.getName());
            roomTotalItemVO.setRoomStatusColor(roomStatusEnum.getColor());
            roomTotalItemVO.setTotal(0);
            result.put(roomStatusEnum.getCode(), roomTotalItemVO);
        }

        query.setRoomStatus(null);

        List<RoomTotalItemVO> statusTotal = roomRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(roomTotalItemVO -> {
            RoomTotalItemVO orDefault = result.getOrDefault(roomTotalItemVO.getRoomStatus(), roomTotalItemVO);
            orDefault.setTotal(roomTotalItemVO.getTotal());
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
        PageVO<RoomItemVO> roomPage = getRoomList(query);

        Map<String, House> allHouseMap = houseRepo.list().stream()
            .collect(Collectors.toMap(House::getHouseCode, house -> house));

        List<RoomItemVO> allRooms = roomPage.getList();

        // 按houseId分组
        Map<String, List<RoomItemVO>> roomsGroupedByHouse = allRooms.stream()
            .collect(Collectors.groupingBy(RoomItemVO::getHouseCode));

        // 处理每个house的数据
        return roomsGroupedByHouse.entrySet().stream()
            .map(entry -> {
                String houseCode = entry.getKey();

                RoomGridVO roomGridVO = new RoomGridVO();
                roomGridVO.setHouseCode(entry.getKey());
                roomGridVO.setHouseId(allHouseMap.get(houseCode).getId());
                roomGridVO.setHouseName(allHouseMap.get(houseCode).getHouseName());
                roomGridVO.setTotal((long) entry.getValue().size());

                List<RoomItemVO> roomsInHouse = entry.getValue();
                // 按floor分组
                Map<Integer, List<RoomItemVO>> roomsGroupedByFloor = roomsInHouse.stream()
                    .collect(Collectors.groupingBy(RoomItemVO::getFloor));

                // 创建楼层列表
                List<RoomGridVO.HouseFloorGridDTO> floorGridList = roomsGroupedByFloor.entrySet().stream()
                    .map(floorEntry -> {
                        Integer floor = floorEntry.getKey();
                        List<RoomItemVO> roomsOnFloor = floorEntry.getValue();

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
    private Pair<Long, BigDecimal> calculateLeasedRateAndCount(List<RoomItemVO> roomsList) {
        // 计算出租率
        long leasedCount = roomsList.stream()
            .map(RoomItemVO::getRoomStatus)
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
