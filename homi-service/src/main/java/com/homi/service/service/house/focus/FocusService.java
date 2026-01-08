package com.homi.service.service.house.focus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.dto.community.CommunityDTO;
import com.homi.model.dto.house.HouseLayoutDTO;
import com.homi.model.focus.dto.FocusBuildingDTO;
import com.homi.model.focus.dto.FocusCreateDTO;
import com.homi.model.focus.dto.FocusHouseDTO;
import com.homi.model.focus.dto.FocusQueryDTO;
import com.homi.model.focus.vo.FocusListVO;
import com.homi.model.focus.vo.FocusTotalVO;
import com.homi.model.vo.IdNameVO;
import com.homi.service.service.room.RoomSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/7/21
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FocusService {
    private final HouseRepo houseRepo;
    private final FocusRepo focusRepo;
    private final FocusBuildingRepo focusBuildingRepo;
    private final RoomRepo roomRepo;
    private final HouseLayoutRepo houseLayoutRepo;
    private final CommunityRepo communityRepo;
    private final DictDataRepo dictDataRepo;

    private final RoomSearchService roomSearchService;


    /**
     * 创建集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/21 22:05
     *
     * @param focusCreateDto 参数说明
     * @return java.lang.Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createHouseFocus(FocusCreateDTO focusCreateDto) {
        if (focusRepo.checkFocusCodeExist(null, focusCreateDto.getFocusCode())) {
            throw new BizException("项目编号（" + focusCreateDto.getFocusCode() + "）已存在");
        }

        Community community = communityRepo.createCommunity(focusCreateDto.getCommunity());
        focusCreateDto.getCommunity().setCommunityId(community.getId());

        // 创建集中式项目
        Focus focus = focusRepo.saveFocus(focusCreateDto);
        focusCreateDto.setId(focus.getId());

        // 创建集中式楼栋信息
        saveFocusBuildings(focus.getId(), focusCreateDto.getBuildings());

        // 创建集中式户型
        Map<Long, Long> houseLayoutIdMap = createOrUpdateFocusHouseLayouts(focusCreateDto);

        // 创建集中式房源
        createFocusHouses(houseLayoutIdMap, focusCreateDto);

        return focus.getId();
    }

    /**
     * 创建集中式房源户型
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/20 13:50
     *
     * @param houseCreateDto 创建房源参数
     * @return java.util.Map<java.lang.Long, java.lang.Long>
     */
    public Map<Long, Long> createOrUpdateFocusHouseLayouts(FocusCreateDTO houseCreateDto) {
        Map<Long, Long> houseLayoutIdMap = new HashMap<>();
        houseCreateDto.getHouseLayoutList().forEach(houseLayoutDTO -> {
            HouseLayout houseLayout = new HouseLayout();
            BeanUtils.copyProperties(houseLayoutDTO, houseLayout, "id");
            houseLayout.setLeaseMode(LeaseModeEnum.FOCUS.getCode());
            houseLayout.setLeaseModeId(houseCreateDto.getId());
            houseLayout.setCompanyId(houseCreateDto.getCompanyId());
            houseLayout.setCreateBy(houseCreateDto.getCreateBy());
            houseLayout.setCreateTime(houseCreateDto.getCreateTime());
            houseLayout.setUpdateBy(houseCreateDto.getUpdateBy());
            houseLayout.setUpdateTime(houseCreateDto.getUpdateTime());

            // 冗余信息
            houseLayout.setFacilities(JSONUtil.toJsonStr(houseLayoutDTO.getFacilities()));
            // 设置标签
            houseLayout.setTags(JSONUtil.toJsonStr(houseLayoutDTO.getTags()));
            houseLayout.setImageList(JSONUtil.toJsonStr(houseLayoutDTO.getImageList()));

            if (houseLayoutDTO.getNewly().equals(Boolean.TRUE)) {
                houseLayoutRepo.getBaseMapper().insert(houseLayout);
            } else {
                houseLayout.setId(houseLayoutDTO.getId());
                houseLayoutRepo.getBaseMapper().updateById(houseLayout);
            }

            houseLayoutIdMap.put(houseLayoutDTO.getId(), houseLayout.getId());
        });

        return houseLayoutIdMap;
    }

    /**
     * 更新集中式的楼栋信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:25
     *
     * @param focusId      参数说明
     * @param buildingList 参数说明
     * @return boolean
     */
    public boolean saveFocusBuildings(Long focusId, List<FocusBuildingDTO> buildingList) {
        List<FocusBuilding> focusBuildings = buildingList.stream().map(building -> {
                FocusBuilding focusBuilding = focusBuildingRepo.getFocusBuilding(focusId, building.getBuilding(), building.getUnit());
                if (Objects.isNull(focusBuilding)) {
                    focusBuilding = BeanCopyUtils.copyBean(building, FocusBuilding.class);
                    Objects.requireNonNull(focusBuilding).setFocusId(focusId);
                } else {
                    BeanUtils.copyProperties(building, focusBuilding);
                }

                return focusBuilding;
            }
        ).toList();

        return focusBuildingRepo.saveOrUpdateBatch(focusBuildings);

    }

    /**
     * 创建集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:18
     *
     * @param houseLayoutIdMap 集中式房型
     * @param focusCreateDto   集中式房源数据
     */
    private void createFocusHouses(Map<Long, Long> houseLayoutIdMap, FocusCreateDTO focusCreateDto) {
        focusCreateDto.getHouseList().forEach(houseDTO -> {
            House house = new House();

            BeanUtils.copyProperties(focusCreateDto, house);
            BeanUtils.copyProperties(houseDTO, house);

            house.setCommunityId(focusCreateDto.getCommunity().getCommunityId());

            // 集中式标记
            house.setLeaseModeId(focusCreateDto.getId());
            house.setLeaseMode(LeaseModeEnum.FOCUS.getCode());

            house.setHouseLayoutId(houseLayoutIdMap.get(houseDTO.getHouseLayoutId()));

            // 创建集中式房源编号
            String houseCode = String.format("%s%s%s%s", focusCreateDto.getFocusCode(), houseDTO.getBuilding(), houseDTO.getUnit(), houseDTO.getDoorNumber());
            house.setHouseCode(houseCode);

            house.setHouseName(String.format("%s%s%s栋%s-%s室", focusCreateDto.getCommunity().getDistrict(),
                focusCreateDto.getCommunity().getName(),
                houseDTO.getBuilding(),
                CharSequenceUtil.isBlank(houseDTO.getUnit()) ? "" : "" + houseDTO.getUnit() + "单元",
                houseDTO.getDoorNumber()));

            houseRepo.saveHouse(house);

            createFocusRoom(house);
        });
    }

    /**
     * 根据房源创建房间
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:50
     *
     * @param house 参数说明
     */
    private void createFocusRoom(House house) {
        Room room = new Room();

        BeanUtils.copyProperties(house, room);

        room.setHouseId(house.getId());
        RoomStatusEnum roomStatusEnum = roomRepo.calculateRoomStatus(room);
        room.setRoomStatus(roomStatusEnum.getCode());
        room.setKeywords(roomSearchService.generateKeywords(room));

        room.setRoomNumber(house.getDoorNumber());

        Room roomBefore = roomRepo.getRoomByHouseIdAndRoomNumber(house.getId(), house.getDoorNumber());
        if (Objects.nonNull(roomBefore)) {
            room.setId(roomBefore.getId());
            roomRepo.getBaseMapper().updateById(room);
        } else {
            room.setCreateBy(house.getCreateBy());
            room.setCreateTime(house.getCreateTime());

            room.setVacancyStartTime(DateUtil.date());
            roomRepo.getBaseMapper().insert(room);
        }
    }

    /**
     * 更新集中式房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:07
     *
     * @param focusCreateDto 参数说明
     * @return java.lang.Long
     */
    @Transactional(rollbackFor = Exception.class)
    public Long updateHouseFocus(FocusCreateDTO focusCreateDto) {
        Optional<Focus> optById = focusRepo.getOptById(focusCreateDto.getId());
        if (optById.isEmpty()) {
            throw new BizException("集中式项目不存在");
        }

        // 创建集中式项目
        Focus focus = focusRepo.saveFocus(focusCreateDto);

        // 创建集中式楼栋信息
        saveFocusBuildings(focus.getId(), focusCreateDto.getBuildings());

        // 创建集中式户型
        Map<Long, Long> houseLayoutIdMap = createOrUpdateFocusHouseLayouts(focusCreateDto);

        // 创建集中式房源
        createFocusHouses(houseLayoutIdMap, focusCreateDto);

        return focus.getId();
    }

    public List<IdNameVO> getFocusOptionList() {
        List<Focus> focusList = focusRepo.list();

        return focusList.stream().map(focus -> IdNameVO.builder()
            .id(focus.getId())
            .name(String.format("%s（%s）", focus.getFocusName(), focus.getFocusCode()))
            .build()).toList();
    }

    public Boolean checkFocusCodeExist(Long id, String focusCode) {
        return focusRepo.checkFocusCodeExist(id, focusCode);
    }

    public FocusCreateDTO getFocusById(Long focusId) {
        Optional<Focus> optById = focusRepo.getOptById(focusId);
        if (optById.isEmpty()) {
            throw new BizException("集中式项目不存在");
        }

        Focus focus = optById.get();

        FocusCreateDTO focusCreateDTO = new FocusCreateDTO();
        BeanUtils.copyProperties(focus, focusCreateDTO);

        focusCreateDTO.setLeaseMode(LeaseModeEnum.FOCUS.getCode());

        CommunityDTO communityDTO = communityRepo.getCommunityById(focus.getCommunityId());
        focusCreateDTO.setCommunity(communityDTO);

        focusCreateDTO.setTags(JSONUtil.toList(focus.getTags(), String.class));
        focusCreateDTO.setFacilities(JSONUtil.toList(focus.getFacilities(), String.class));
        focusCreateDTO.setImageList(JSONUtil.toList(focus.getImageList(), String.class));

        List<FocusBuilding> focusBuildings = focusBuildingRepo.getBuildingsByFocusId(focus.getId());
        List<FocusBuildingDTO> list = focusBuildings.stream()
            .map(focusBuilding -> BeanCopyUtils.copyBean(focusBuilding, FocusBuildingDTO.class))
            .toList();
        focusCreateDTO.setBuildings(list);

        List<HouseLayoutDTO> layoutListByLeaseModeId = houseLayoutRepo.getLayoutListByLeaseModeId(focusId, LeaseModeEnum.FOCUS.getCode());
        focusCreateDTO.setHouseLayoutList(layoutListByLeaseModeId);

        List<House> focusHouses = houseRepo.getHousesByLeaseModeId(focusId, LeaseModeEnum.FOCUS.getCode());
        List<FocusHouseDTO> houseList = focusHouses.stream()
            .map(focusHouse -> BeanCopyUtils.copyBean(focusHouse, FocusHouseDTO.class))
            .toList();
        focusCreateDTO.setHouseList(houseList);

        return focusCreateDTO;
    }

    public PageVO<FocusListVO> getFocusList(FocusQueryDTO query) {
        IPage<Focus> pageQuery = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<Focus> wrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(query.getKeywords())) {
            wrapper.like(Focus::getFocusName, query.getKeywords());
        }
        if (Objects.nonNull(query.getLeaseModeId())) {
            wrapper.eq(Focus::getId, query.getLeaseModeId());
        }

        IPage<Focus> focusPage = focusRepo.page(pageQuery, wrapper);

        // 封装返回结果
        PageVO<FocusListVO> pageVO = new PageVO<>();
        pageVO.setTotal(focusPage.getTotal());
        pageVO.setList(focusPage.getRecords().stream().map(this::formatFocusList).toList());
        pageVO.setCurrentPage(focusPage.getCurrent());
        pageVO.setPageSize(focusPage.getSize());
        pageVO.setPages(focusPage.getPages());

        return pageVO;
    }

    private FocusListVO formatFocusList(Focus focus) {
        FocusListVO focusListVO = BeanCopyUtils.copyBean(focus, FocusListVO.class);
        assert focusListVO != null;

        focusListVO.setTags(JSONUtil.toList(focus.getTags(), String.class));
        focusListVO.setFacilities(JSONUtil.toList(focus.getFacilities(), String.class));

        if (CollUtil.isNotEmpty(focusListVO.getFacilities())) {
            // 设施名称
            focusListVO.setFacilityNames(dictDataRepo.getDictDataListByCodes(focusListVO.getFacilities()).stream()
                .map(DictData::getName).toList());
        }

        focusListVO.setImageList(JSONUtil.toList(focus.getImageList(), String.class));

        CommunityDTO communityDTO = communityRepo.getCommunityById(focus.getCommunityId());
        focusListVO.setCommunity(communityDTO);

        // 统计出租率
        long totalCount = houseRepo.count(new LambdaQueryWrapper<House>()
            .eq(House::getLeaseModeId, focus.getId())
            .eq(House::getLeaseMode, LeaseModeEnum.FOCUS.getCode()));


        // 统计某个集中式项目下所有房源的剩余房间数量总和
        Integer totalRentedRoomCount = houseRepo.getBaseMapper().getTotalRentedRoomCount(focus.getId(), LeaseModeEnum.FOCUS.getCode());

        double occupancyRate = totalCount > 0 ? (double) totalRentedRoomCount / totalCount * 100 : 0;

        FocusTotalVO focusTotalVOBuilder = FocusTotalVO.builder()
            .totalRoomCount(totalCount)
            .totalRentedRoomCount(Long.valueOf(totalRentedRoomCount))
            .occupancyRate(occupancyRate)
            .build();


        focusListVO.setFocusTotal(focusTotalVOBuilder);

        return focusListVO;
    }
}
