package com.homi.service.service.room;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.model.dao.entity.Community;
import com.homi.model.dao.entity.Focus;
import com.homi.model.dao.repo.CommunityRepo;
import com.homi.model.dao.repo.FocusRepo;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.model.room.dto.RoomQueryDTO;
import com.homi.model.room.dto.grid.RoomGridDTO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.model.room.vo.grid.*;
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
    private final CommunityRepo communityRepo;
    private final FocusRepo focusRepo;

    private final RoomService roomService;

    /**
     * 查询小区的聚合房间数据，根据小区、楼栋、单元、楼层来聚合显示数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/29 14:27
     *
     * @param query 参数说明
     * @return java.util.List<com.homi.domain.vo.room.grid.RoomAggregatedVO>
     */
    public List<RoomAggregatedVO> getAggregatedRooms(RoomQueryDTO query) {
        return roomRepo.selectAggregatedRooms(query);
    }

    public record RoomGridGroupKey(Long leaseModeId, Integer leaseMode, String building, String unit, Integer floor) implements Comparable<RoomGridGroupKey> {
        @Override
        public int compareTo(RoomGridGroupKey o) {
            int leaseModeIdComparison = Long.compare(this.leaseModeId, o.leaseModeId);
            if (leaseModeIdComparison != 0) {
                return leaseModeIdComparison;
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
        List<RoomAggregatedVO> aggregatedRooms = getAggregatedRooms(query);

        // 4. 获取当前页的小区、楼栋单元和楼层信息
        long offset = (query.getCurrentPage() - 1) * query.getPageSize();
        List<RoomAggregatedVO> currentQueryStatistic = aggregatedRooms.stream().skip(offset).limit(query.getPageSize()).toList();

        result.setCurrentPage(query.getCurrentPage());
        result.setPageSize(query.getPageSize());
        result.setHasMore(offset + query.getPageSize() < aggregatedRooms.size());

        // 5. 查询分页的小区（项目）、楼栋、单元、楼层的房间数据
        query.setSpatialQuery(currentQueryStatistic);
        IPage<RoomListVO> roomItemDTOIPage = roomRepo.pageRoomGridList(query);
        List<RoomListVO> rooms = roomItemDTOIPage.getRecords();

        // 7. 构建楼层分组数据
        Map<RoomGridGroupKey, List<RoomListVO>> roomGridGroupKeyListMap = rooms.stream().collect(Collectors.groupingBy(
            room -> new RoomGridGroupKey(
                room.getLeaseModeId(),
                room.getLeaseMode(),
                room.getBuilding(),
                room.getUnit(),
                room.getFloor()
            )
        ));

        // 8. 构建返回结果
        List<RoomGridItemVO> roomGridItemList = new ArrayList<>();
        for (Map.Entry<RoomGridGroupKey, List<RoomListVO>> entry : roomGridGroupKeyListMap.entrySet()) {
            RoomGridItemVO roomGridItemVO = new RoomGridItemVO();
            RoomGridGroupKey key = entry.getKey();

            CompoundGroup compoundGroup = getCompoundGroup(key.leaseModeId, key.leaseMode, aggregatedRooms);
            roomGridItemVO.setCompoundGroup(compoundGroup);
            roomGridItemVO.setBuildingGroup(getBuildingGroup(key.leaseModeId, key.building, key.unit, aggregatedRooms));
            roomGridItemVO.setFloorGroup(getFloorGroup(key.leaseModeId, key.building, key.unit, key.floor, aggregatedRooms));

            // 翻译房间状态
            entry.getValue().forEach(room -> {
                RoomStatusEnum roomStatusEnum = EnumUtil.getBy(RoomStatusEnum::getCode, room.getRoomStatus());
                room.setRoomStatusName(roomStatusEnum.getName());
                room.setRoomStatusColor(roomStatusEnum.getColor());

                // 如果房间是已租时，获取当前租客的租约信息
                roomService.getRoomLeaseInfo(room);
            });

            roomGridItemVO.setRooms(entry.getValue());

            roomGridItemList.add(roomGridItemVO);
        }

        // 按照 community 倒序、unitGroup 正序、floor 正序排序
        roomGridItemList.sort(Comparator.comparing((RoomGridItemVO item) -> -item.getCompoundGroup().getCommunityId())
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
     * @return com.homi.domain.vo.room.grid.FloorGroup
     */
    private FloorGroup getFloorGroup(Long modRefId, String building, String unit, Integer floor, List<RoomAggregatedVO> aggregatedRooms) {
        FloorGroup floorGroup = new FloorGroup();
        floorGroup.setFloor(floor);

        int roomCount = 0;
        int leasedCount = 0;

        for (RoomAggregatedVO room : aggregatedRooms) {
            if (modRefId.equals(room.getLeaseModeId()) && building.equals(room.getBuilding()) && unit.equals(room.getUnit()) && floor.equals(room.getFloor())) {
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
     * @param leaseModeId     参数说明
     * @param building        参数说明
     * @param unit            参数说明
     * @param aggregatedRooms 参数说明
     * @return com.homi.domain.vo.room.grid.BuildingGroup
     */
    private BuildingGroup getBuildingGroup(Long leaseModeId, String building, String unit, List<RoomAggregatedVO> aggregatedRooms) {
        BuildingGroup buildingGroup = new BuildingGroup();
        buildingGroup.setBuilding(building);
        buildingGroup.setUnit(unit);

        int roomCount = 0;
        int leasedCount = 0;

        for (RoomAggregatedVO room : aggregatedRooms) {
            if (leaseModeId.equals(room.getLeaseModeId()) && building.equals(room.getBuilding()) && unit.equals(room.getUnit())) {
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
     * @param leaseModeId     leaseMode=集中式时，为集中式id；leaseMode=分散式时，为小区 id
     * @param leaseMode       租房模式
     * @param aggregatedRooms 参数说明
     * @return com.homi.domain.vo.room.grid.CompoundGroup
     */
    public CompoundGroup getCompoundGroup(Long leaseModeId, Integer leaseMode, List<RoomAggregatedVO> aggregatedRooms) {
        CompoundGroup compoundGroup = new CompoundGroup();
        compoundGroup.setLeaseModeId(leaseModeId);
        compoundGroup.setLeaseMode(leaseMode);

        Long communityId;
        String displayName = CharSequenceUtil.EMPTY;
        if (leaseMode.equals(LeaseModeEnum.FOCUS.getCode())) {
            Focus focus = focusRepo.getById(leaseModeId);
            displayName = focus.getFocusName();
            communityId = focus.getCommunityId();
        } else {
            communityId = leaseModeId;
        }

        Community community = communityRepo.getById(communityId);
        compoundGroup.setCommunityId(communityId);
        compoundGroup.setCommunityName(community.getName());
        compoundGroup.setCommunityAddress(String.format("%s（%s%s）", community.getName(), community.getDistrict(), community.getAddress()));

        compoundGroup.setDisplayName(CharSequenceUtil.blankToDefault(displayName, community.getName()));

        Set<String> buildings = new HashSet<>();
        Set<Integer> floors = new HashSet<>();
        int roomCount = 0;
        int leasedCount = 0;

        for (RoomAggregatedVO room : aggregatedRooms) {
            if (leaseModeId.equals(room.getLeaseModeId())) {
                Optional.ofNullable(room.getBuilding()).ifPresent(buildings::add);
                Optional.ofNullable(room.getFloor()).ifPresent(floors::add);
                roomCount += Optional.ofNullable(room.getRoomCount()).orElse(0);
                leasedCount += Optional.ofNullable(room.getLeasedCount()).orElse(0);
            }
        }

        compoundGroup.setBuildingCount(buildings.size());
        compoundGroup.setFloorCount(floors.size());

        compoundGroup.setRoomCount(roomCount);
        compoundGroup.setLeasedCount(leasedCount);

        compoundGroup.setOccupancyRate(
            BigDecimal.valueOf(compoundGroup.getLeasedCount() * 100.0 / compoundGroup.getRoomCount())
                .setScale(2, RoundingMode.HALF_UP)
        );

        return compoundGroup;
    }
}
