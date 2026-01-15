package com.homi.service.service.house.scatter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.community.dto.CommunityDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import com.homi.model.house.dto.HouseLayoutDTO;
import com.homi.model.room.dto.RoomDetailDTO;
import com.homi.model.scatter.ScatterCreateDTO;
import com.homi.model.scatter.ScatterHouseVO;
import com.homi.service.service.house.HouseCodeGenerator;
import com.homi.service.service.price.PriceConfigService;
import com.homi.service.service.room.RoomSearchService;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/23
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ScatterService {
    private final HouseRepo houseRepo;
    private final CommunityRepo communityRepo;
    private final RoomRepo roomRepo;
    private final HouseLayoutRepo houseLayoutRepo;
    private final FileMetaRepo fileMetaRepo;
    private final CompanyRepo companyRepo;

    private final HouseCodeGenerator houseCodeGenerator;

    private final RoomService roomService;
    private final PriceConfigService priceConfigService;
    private final RoomSearchService roomSearchService;

    /**
     * 创建整租房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/23 10:19
     *
     * @param scatterCreateDTO 参数说明
     * @return java.lang.Long
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrUpdateHouse(ScatterCreateDTO scatterCreateDTO) {
        Community community = communityRepo.createCommunityIfNotExist(scatterCreateDTO.getCommunity());
        scatterCreateDTO.getCommunity().setCommunityId(community.getId());

        // 创建整租房源
        createOrUpdateHouseList(scatterCreateDTO);

        // 设置上传文件为已使用
        if (scatterCreateDTO.getHouseList() != null) {
            List<String> imageList = scatterCreateDTO.getHouseList().stream()
                .filter(h -> h.getHouseLayout() != null && h.getHouseLayout().getImageList() != null)
                .flatMap(h -> h.getHouseLayout().getImageList().stream())
                .collect(Collectors.toList());

            if (!imageList.isEmpty()) { // 确保不为空才创建 Optional
                Optional.of(imageList).ifPresent(strings -> fileMetaRepo.setFileUsedByName(strings));
            }
        }

        return Boolean.TRUE;
    }

    private void createOrUpdateHouseList(ScatterCreateDTO scatterCreateDTO) {
        Company company = companyRepo.getById(scatterCreateDTO.getCompanyId());
        String companyCode = company.getCode();

        scatterCreateDTO.getHouseList().forEach(houseDTO -> {
            // 检查房源是否存在
            Boolean exist = houseRepo.checkHouseExist(houseDTO.getId(), scatterCreateDTO.getCommunity().getCommunityId(),
                houseDTO.getBuilding(), houseDTO.getUnit(), houseDTO.getDoorNumber());
            if (Boolean.TRUE.equals(exist)) {
                throw new IllegalArgumentException(houseDTO.getBuilding() + houseDTO.getUnit() + houseDTO.getDoorNumber() + " 已存在！");
            }

            House house = BeanCopyUtils.copyBean(houseDTO, House.class);
            assert house != null;
            house.setId(houseDTO.getId());

            if (Objects.isNull(houseDTO.getId())) {
                // 生成房源编码
                String houseCode = houseCodeGenerator.generate(companyCode);
                house.setHouseCode(houseCode);
            }

            String address = String.format("%s%s%s栋%s-%s室", scatterCreateDTO.getCommunity().getDistrict(),
                scatterCreateDTO.getCommunity().getName(),
                houseDTO.getBuilding(),
                CharSequenceUtil.isBlank(houseDTO.getUnit()) ? "" : houseDTO.getUnit() + "单元",
                houseDTO.getDoorNumber());
            house.setHouseName(address);

            /*
             * 创建户型数据
             */
            Long layoutId = createOrUpdateScatterHouseLayout(scatterCreateDTO, houseDTO.getHouseLayout());
            house.setHouseLayoutId(layoutId);
            // 设置租赁类型
            house.setLeaseMode(LeaseModeEnum.SCATTER.getCode());
            house.setLeaseModeId(scatterCreateDTO.getCommunity().getCommunityId());
            house.setCommunityId(scatterCreateDTO.getCommunity().getCommunityId());
            house.setWater(scatterCreateDTO.getWater());
            house.setElectricity(scatterCreateDTO.getElectricity());
            house.setHeating(scatterCreateDTO.getHeating());
            house.setHasElevator(scatterCreateDTO.getHasElevator());
            house.setDeptId(scatterCreateDTO.getDeptId());
            house.setSalesmanId(scatterCreateDTO.getSalesmanId());

            if (Objects.nonNull(house.getId())) {
                house.setUpdateBy(scatterCreateDTO.getCreateBy());
                house.setUpdateTime(scatterCreateDTO.getCreateTime());
                houseRepo.updateById(house);
            } else {
                house.setCreateBy(scatterCreateDTO.getCreateBy());
                house.setCreateTime(scatterCreateDTO.getCreateTime());

                houseRepo.save(house);
                houseDTO.setId(house.getId());
            }

            /*
             * 整租和合租房间都使用按照同一个逻辑来处理；把整租理解为一个房间，合租理解为多个房间。
             * <p>
             * {@code @author} tk
             * {@code @date} 2026/1/15 16:10
             * @param scatterCreateDTO 参数说明
             */
            createScatterRoomList(house, houseDTO.getRoomList());
        });
    }

    public Long createOrUpdateScatterHouseLayout(ScatterCreateDTO scatterCreateDTO, HouseLayoutDTO houseLayoutDTO) {
        HouseLayout houseLayout = new HouseLayout();
        BeanUtils.copyProperties(houseLayoutDTO, houseLayout, "id");
        houseLayout.setLeaseMode(LeaseModeEnum.SCATTER.getCode());
        houseLayout.setLeaseModeId(scatterCreateDTO.getCommunity().getCommunityId());
        houseLayout.setCompanyId(scatterCreateDTO.getCompanyId());

        houseLayout.setFacilities(JSONUtil.toJsonStr(houseLayoutDTO.getFacilities()));
        // 设置标签
        houseLayout.setTags(JSONUtil.toJsonStr(houseLayoutDTO.getTags()));
        houseLayout.setImageList(JSONUtil.toJsonStr(houseLayoutDTO.getImageList()));
        houseLayout.setVideoList(JSONUtil.toJsonStr(houseLayoutDTO.getVideoList()));

        if (Objects.isNull(houseLayoutDTO.getId())) {
            houseLayout.setCreateBy(scatterCreateDTO.getCreateBy());
            houseLayoutRepo.getBaseMapper().insert(houseLayout);
        } else {
            houseLayout.setId(houseLayoutDTO.getId());
            houseLayout.setUpdateBy(scatterCreateDTO.getUpdateBy());
            houseLayoutRepo.getBaseMapper().updateById(houseLayout);
        }

        return houseLayout.getId();
    }

    /**
     * 设置价格
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/23 15:40
     *
     * @param house         参数说明
     * @param roomDetailDTO 参数说明
     */
    public void createOrUpdateScatterRoom(House house, RoomDetailDTO roomDetailDTO) {
        Room room = new Room();

        BeanUtils.copyProperties(roomDetailDTO, room);

        room.setHouseId(house.getId());
        room.setFloor(house.getFloor());
        RoomStatusEnum roomStatusEnum = roomRepo.calculateRoomStatus(room);
        room.setRoomStatus(roomStatusEnum.getCode());
        room.setKeywords(roomSearchService.generateKeywords(room));

        room.setUpdateBy(house.getUpdateBy());

        Room roomBefore = roomRepo.getRoomByHouseIdAndRoomNumber(house.getId(), roomDetailDTO.getRoomNumber());
        if (Objects.nonNull(roomBefore)) {
            room.setId(roomBefore.getId());
            roomRepo.updateById(room);
        } else {
            room.setCreateBy(house.getCreateBy());
            room.setCreateTime(house.getCreateTime());
            room.setVacancyStartTime(DateUtil.date());
            roomRepo.save(room);
        }

        roomDetailDTO.getPriceConfig().setRoomId(room.getId());
        priceConfigService.createOrUpdatePriceConfig(roomDetailDTO.getPriceConfig());
    }

    public void createScatterRoomList(House house, List<RoomDetailDTO> roomList) {
        roomList.forEach(roomDetailDTO -> createOrUpdateScatterRoom(house, roomDetailDTO));
    }

    public ScatterHouseVO getScatterId(Long houseId) {
        House house = houseRepo.getById(houseId);
        ScatterHouseVO scatterHouseVO = new ScatterHouseVO();
        BeanUtils.copyProperties(house, scatterHouseVO);

        // 加载小区数据
        CommunityDTO communityDTO = communityRepo.getCommunityById(house.getCommunityId());
        scatterHouseVO.setCommunity(communityDTO);

        // 加载户型数据
        HouseLayoutDTO houseLayoutById = houseLayoutRepo.getHouseLayoutById(house.getHouseLayoutId());
        scatterHouseVO.setHouseLayout(houseLayoutById);

        List<RoomDetailDTO> roomList = roomService.getRoomListByHouseId(house.getId());

        scatterHouseVO.setRoomList(roomList);

        return scatterHouseVO;
    }
}
