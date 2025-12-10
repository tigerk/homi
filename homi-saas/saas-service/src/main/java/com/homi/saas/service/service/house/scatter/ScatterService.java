package com.homi.saas.service.service.house.scatter;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.homi.model.dao.repo.*;
import com.homi.model.dto.community.CommunityDTO;
import com.homi.model.dto.house.HouseLayoutDTO;
import com.homi.model.dto.house.scatter.ScatterCreateDTO;
import com.homi.model.dto.room.RoomDetailDTO;
import com.homi.common.lib.enums.house.LeaseModeEnum;
import com.homi.common.lib.enums.house.RentalTypeEnum;
import com.homi.model.vo.house.ScatterHouseVO;
import com.homi.model.dao.entity.Community;
import com.homi.model.dao.entity.Company;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.HouseLayout;
import com.homi.saas.service.service.house.HouseCodeGenerator;
import com.homi.saas.service.service.room.RoomSearchService;
import com.homi.saas.service.service.room.RoomService;
import jakarta.annotation.Resource;
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
    @Resource
    private HouseRepo houseRepo;

    @Resource
    private CommunityRepo communityRepo;

    @Resource
    private RoomRepo roomRepo;

    @Resource
    private HouseLayoutRepo houseLayoutRepo;

    @Resource
    private RoomSearchService roomSearchService;

    @Resource
    private FileMetaRepo fileMetaRepo;

    @Resource
    private EntireService entireService;

    @Resource
    private ShareService shareService;

    @Resource
    private HouseCodeGenerator houseCodeGenerator;

    @Resource
    private CompanyRepo companyRepo;

    @Resource
    private RoomService roomService;

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
    public Boolean createHouse(ScatterCreateDTO scatterCreateDTO) {
        Community community = communityRepo.createCommunity(scatterCreateDTO.getCommunity());
        scatterCreateDTO.getCommunity().setCommunityId(community.getId());

        // 创建整租房源
        createHouseList(scatterCreateDTO);

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

    private void createHouseList(ScatterCreateDTO scatterCreateDTO) {
        Company company = companyRepo.getById(scatterCreateDTO.getCompanyId());
        String companyCode = company.getCode();

        scatterCreateDTO.getHouseList().forEach(houseDTO -> {
            String address = String.format("%s%s%s栋%s-%s室", scatterCreateDTO.getCommunity().getDistrict(),
                scatterCreateDTO.getCommunity().getName(),
                houseDTO.getBuilding(),
                CharSequenceUtil.isBlank(houseDTO.getUnit()) ? "" : houseDTO.getUnit() + "单元",
                houseDTO.getDoorNumber());

            Boolean exist = houseRepo.checkHouseExist(scatterCreateDTO.getCommunity().getCommunityId(), houseDTO.getBuilding(), houseDTO.getUnit(), houseDTO.getDoorNumber());
            if (Boolean.TRUE.equals(exist)) {
                throw new IllegalArgumentException(address + " 已存在！");
            }

            House house = new House();
            house.setId(houseDTO.getId());

            // 生成房源编码
            String houseCode = houseCodeGenerator.generate(companyCode);
            house.setHouseCode(houseCode);

            /*
             * 创建户型数据
             */
            Long layoutId = createScatterHouseLayout(scatterCreateDTO, houseDTO.getHouseLayout());
            house.setHouseLayoutId(layoutId);

            house.setLeaseMode(LeaseModeEnum.SCATTER.getCode());
            house.setModeRefId(scatterCreateDTO.getCommunity().getCommunityId());
            // 设置租赁类型
            house.setRentalType(houseDTO.getRentalType());

            house.setCommunityId(scatterCreateDTO.getCommunity().getCommunityId());
            house.setBuilding(houseDTO.getBuilding());
            house.setUnit(houseDTO.getUnit());
            house.setDoorNumber(houseDTO.getDoorNumber());
            house.setFloor(houseDTO.getFloor());
            house.setFloorTotal(houseDTO.getFloorTotal());
            house.setArea(houseDTO.getArea());
            house.setDirection(houseDTO.getDirection());
            house.setWater(scatterCreateDTO.getWater());
            house.setElectricity(scatterCreateDTO.getElectricity());
            house.setHeating(scatterCreateDTO.getHeating());
            house.setHasElevator(scatterCreateDTO.getHasElevator());

            house.setDeptId(scatterCreateDTO.getDeptId());
            house.setSalesmanId(scatterCreateDTO.getSalesmanId());

            house.setHouseName(address);

            house.setUpdateBy(scatterCreateDTO.getCreateBy());
            house.setUpdateTime(scatterCreateDTO.getCreateTime());

            if (Objects.nonNull(house.getId())) {
                houseRepo.updateById(house);
            } else {
                house.setCreateBy(scatterCreateDTO.getCreateBy());
                house.setCreateTime(scatterCreateDTO.getCreateTime());

                houseRepo.save(house);
                houseDTO.setId(house.getId());
            }

            /*
             * 创建整租房源 or 合租房间
             */
            if (house.getRentalType().equals(RentalTypeEnum.ENTIRE.getCode())) {
                entireService.createEntireRoom(house, houseDTO.getPrice(), houseDTO.getPriceConfig());
            } else {
                shareService.createShareRoom(house, houseDTO.getRoomList());
            }
        });
    }

    public Long createScatterHouseLayout(ScatterCreateDTO scatterCreateDTO, HouseLayoutDTO houseLayoutDTO) {
        HouseLayout houseLayout = new HouseLayout();
        BeanUtils.copyProperties(houseLayoutDTO, houseLayout, "id");
        houseLayout.setLeaseMode(LeaseModeEnum.SCATTER.getCode());
        houseLayout.setModeRefId(scatterCreateDTO.getCommunity().getCommunityId());
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

    public ScatterHouseVO getScatterId(Long houseId) {
        House house = houseRepo.getById(houseId);
        ScatterHouseVO scatterHouseVO = new ScatterHouseVO();
        BeanUtils.copyProperties(house, scatterHouseVO);

        CommunityDTO communityDTO = communityRepo.getCommunityById(house.getCommunityId());
        scatterHouseVO.setCommunity(communityDTO);

        HouseLayoutDTO houseLayoutById = houseLayoutRepo.getHouseLayoutById(house.getHouseLayoutId());
        scatterHouseVO.setHouseLayout(houseLayoutById);

        List<RoomDetailDTO> roomList = roomService.getRoomListByHouseId(house.getId());

        scatterHouseVO.setRoomList(roomList);

        return scatterHouseVO;
    }
}
