package com.homi.service.room;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.dto.room.grid.*;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.domain.enums.house.LeaseModeEnum;
import com.homi.model.entity.Community;
import com.homi.model.entity.Focus;
import com.homi.model.mapper.HouseMapper;
import com.homi.model.repo.CommunityRepo;
import com.homi.model.repo.FocusRepo;
import com.homi.model.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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
    private final FocusRepo focusRepo;

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

    public record RoomGridGroupKey(Long modeRefId, Integer leaseMode, String building, String unit, Integer floor) implements Comparable<RoomGridGroupKey> {
        @Override
        public int compareTo(RoomGridGroupKey o) {
            int modeRefIdComparison = Long.compare(this.modeRefId, o.modeRefId);
            if (modeRefIdComparison != 0) {
                return modeRefIdComparison;
            }
            int leaseModeComparison = Integer.compare(this.leaseMode, o.leaseMode);
            if (leaseModeComparison != 0) {
                return leaseModeComparison;
            }
            int buildingComparison = this.building.compareTo(o.building);
            if (buildingComparison != 0) {
                return buildingComparison;
            }
            int unitComparison = this.unit.compareTo(o.unit);
            if (unitComparison != 0) {
                return unitComparison;
            }

            return this.floor.compareTo(o.floor);
        }
    }

    /**
     * 获取聚合的房间数据
     */
    @Transactional(readOnly = true)
    public RoomGridDTO getRoomGrid(RoomQueryDTO query) {
        RoomGridDTO result = new RoomGridDTO();

        // 一次只显示三个楼层的数据
        List<RoomAggregatedDTO> aggregatedRooms = getAggregatedRooms(query);

        // 4. 获取当前页的小区、楼栋单元和楼层信息
        long offset = (query.getCurrentPage() - 1) * query.getPageSize();
        List<RoomAggregatedDTO> currentQueryStatistic = aggregatedRooms.stream().skip(offset).limit(query.getPageSize()).toList();

        result.setCurrentPage(query.getCurrentPage());
        result.setPageSize(query.getPageSize());
        result.setHasMore(offset + query.getPageSize() < aggregatedRooms.size());

        // 5. 查询分页的小区（项目）、楼栋、单元、楼层的房间数据
        query.setSpatialQuery(currentQueryStatistic);
        IPage<RoomItemDTO> roomItemDTOIPage = roomRepo.pageRoomGridList(query);
        List<RoomItemDTO> rooms = roomItemDTOIPage.getRecords();

        // 7. 构建楼层分组数据
        Map<RoomGridGroupKey, List<RoomItemDTO>> roomGridGroupKeyListMap = rooms.stream().collect(Collectors.groupingBy(
                room -> new RoomGridGroupKey(
                        room.getModeRefId(),
                        room.getLeaseMode(),
                        room.getBuilding(),
                        room.getUnit(),
                        room.getFloor()
                )
        ));

        // 8. 构建返回结果
        List<RoomGridItemDTO> roomGridItemList = new ArrayList<>();
        for (Map.Entry<RoomGridGroupKey, List<RoomItemDTO>> entry : roomGridGroupKeyListMap.entrySet()) {
            RoomGridItemDTO roomGridItemDTO = new RoomGridItemDTO();
            RoomGridGroupKey key = entry.getKey();

            AreaGroup areaGroup = getAreaGroup(key.modeRefId, key.leaseMode, aggregatedRooms);
            roomGridItemDTO.setAreaGroup(areaGroup);
            roomGridItemDTO.setBuildingGroup(getBuildingGroup(key.modeRefId, key.building, key.unit, aggregatedRooms));
            roomGridItemDTO.setFloorGroup(getFloorGroup(key.modeRefId, key.building, key.unit, key.floor, aggregatedRooms));

            // 翻译房间状态
            entry.getValue().forEach(room -> {
                RoomStatusEnum roomStatusEnum = EnumUtil.getBy(RoomStatusEnum::getCode, room.getRoomStatus());
                room.setRoomStatusName(roomStatusEnum.getName());
                room.setRoomStatusColor(roomStatusEnum.getColor());
            });

            roomGridItemDTO.setRooms(entry.getValue());

            roomGridItemList.add(roomGridItemDTO);
        }

        // 按照 community 倒序、unitGroup 正序、floor 正序排序
        roomGridItemList.sort(Comparator.comparing((RoomGridItemDTO item) -> -item.getAreaGroup().getCommunityId())
                .thenComparing(item -> item.getBuildingGroup().getBuilding())
                .thenComparing(item -> item.getBuildingGroup().getUnit())
                .thenComparing(item -> item.getFloorGroup().getFloor()));


        result.setRoomGridItemList(roomGridItemList);

        return result;
    }

    /**
     * 获取楼层的聚合数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/30 09:58
     *
     * @param modRefId        参数说明
     * @param building        参数说明
     * @param unit            参数说明
     * @param floor           参数说明
     * @param aggregatedRooms 参数说明
     * @return com.homi.domain.dto.room.grid.FloorGroup
     */
    private FloorGroup getFloorGroup(Long modRefId, String building, String unit, Integer floor, List<RoomAggregatedDTO> aggregatedRooms) {
        FloorGroup floorGroup = new FloorGroup();
        floorGroup.setFloor(floor);

        int roomCount = 0;
        int leasedCount = 0;

        for (RoomAggregatedDTO room : aggregatedRooms) {
            if (modRefId.equals(room.getModeRefId()) && building.equals(room.getBuilding()) && unit.equals(room.getUnit()) && floor.equals(room.getFloor())) {
                roomCount += (room.getRoomCount() != null ? room.getRoomCount() : 0);
                leasedCount += (room.getLeasedCount() != null ? room.getLeasedCount() : 0);
            }
        }

        floorGroup.setRoomCount(roomCount);
        floorGroup.setLeasedCount(leasedCount);

        floorGroup.setOccupancyRate(BigDecimal.valueOf(floorGroup.getLeasedCount() * 100.0 / floorGroup.getRoomCount()).setScale(2, RoundingMode.HALF_UP)
        );

        return floorGroup;
    }

    /**
     * 获取楼栋单元的聚合数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/30 09:43
     *
     * @param modeRefId       参数说明
     * @param building        参数说明
     * @param unit            参数说明
     * @param aggregatedRooms 参数说明
     * @return com.homi.domain.dto.room.grid.BuildingGroup
     */
    private BuildingGroup getBuildingGroup(Long modeRefId, String building, String unit, List<RoomAggregatedDTO> aggregatedRooms) {
        BuildingGroup buildingGroup = new BuildingGroup();
        buildingGroup.setBuilding(building);
        buildingGroup.setUnit(unit);

        int roomCount = 0;
        int leasedCount = 0;

        for (RoomAggregatedDTO room : aggregatedRooms) {
            if (modeRefId.equals(room.getModeRefId()) && building.equals(room.getBuilding()) && unit.equals(room.getUnit())) {
                roomCount += (room.getRoomCount() != null ? room.getRoomCount() : 0);
                leasedCount += (room.getLeasedCount() != null ? room.getLeasedCount() : 0);
            }
        }

        buildingGroup.setRoomCount(roomCount);
        buildingGroup.setLeasedCount(leasedCount);

        buildingGroup.setOccupancyRate(
                BigDecimal.valueOf(buildingGroup.getLeasedCount() * 100.0 / buildingGroup.getRoomCount())
                        .setScale(2, RoundingMode.HALF_UP)
        );

        return buildingGroup;
    }

    /**
     * 获取小区的单元分组数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/30 09:43
     *
     * @param modeRefId       leaseMode=集中式时，为集中式id；leaseMode=分散式时，为小区 id
     * @param leaseMode       租房模式
     * @param aggregatedRooms 参数说明
     * @return com.homi.domain.dto.room.grid.AreaGroup
     */
    public AreaGroup getAreaGroup(Long modeRefId, Integer leaseMode, List<RoomAggregatedDTO> aggregatedRooms) {
        AreaGroup areaGroup = new AreaGroup();
        areaGroup.setModeRefId(modeRefId);
        areaGroup.setLeaseMode(leaseMode);

        Long communityId;
        String displayName = CharSequenceUtil.EMPTY;
        if (leaseMode.equals(LeaseModeEnum.FOCUS.getCode())) {
            Focus focus = focusRepo.getById(modeRefId);
            displayName = focus.getFocusName();
            communityId = focus.getCommunityId();
        } else {
            communityId = modeRefId;
        }

        Community community = communityRepo.getById(communityId);
        areaGroup.setCommunityId(communityId);
        areaGroup.setCommunityName(community.getName());
        areaGroup.setCommunityAddress(community.getAddress());

        areaGroup.setDisplayName(CharSequenceUtil.blankToDefault(displayName, community.getName()));

        Set<String> buildings = new HashSet<>();
        Set<Integer> floors = new HashSet<>();
        int roomCount = 0;
        int leasedCount = 0;

        for (RoomAggregatedDTO room : aggregatedRooms) {
            if (modeRefId.equals(room.getModeRefId())) {
                Optional.ofNullable(room.getBuilding()).ifPresent(buildings::add);
                Optional.ofNullable(room.getFloor()).ifPresent(floors::add);
                roomCount += Optional.ofNullable(room.getRoomCount()).orElse(0);
                leasedCount += Optional.ofNullable(room.getLeasedCount()).orElse(0);
            }
        }

        areaGroup.setBuildingCount(buildings.size());
        areaGroup.setFloorCount(floors.size());

        areaGroup.setRoomCount(roomCount);
        areaGroup.setLeasedCount(leasedCount);

        areaGroup.setOccupancyRate(
                BigDecimal.valueOf(areaGroup.getLeasedCount() * 100.0 / areaGroup.getRoomCount())
                        .setScale(2, RoundingMode.HALF_UP)
        );

        return areaGroup;
    }
}
