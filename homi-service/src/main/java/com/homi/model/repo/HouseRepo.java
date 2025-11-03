package com.homi.model.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.community.CommunityDTO;
import com.homi.domain.dto.house.scatter.ScatterHouseDTO;
import com.homi.model.entity.House;
import com.homi.model.entity.Room;
import com.homi.model.mapper.HouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 房源表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
@RequiredArgsConstructor
public class HouseRepo extends ServiceImpl<HouseMapper, House> {
    private final RoomRepo roomRepo;

    public boolean updateHouseRoomCount(Long houseId) {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getHouseId, houseId);
        long roomCount = roomRepo.count(queryWrapper);

        queryWrapper.eq(Room::getLeased, false);
        long restCount = roomRepo.count(queryWrapper);

        House house = new House();
        house.setId(houseId);
        house.setRoomCount((int) roomCount);
        house.setRestRoomCount((int) restCount);

        return updateById(house);
    }

    /**
     * 根据houseCode判断是否存在，存在更新，不存在则插入
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:45
     *
     * @param house 参数说明
     */
    public void saveHouse(House house) {
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(House::getHouseCode, house.getHouseCode());

        House oneHouse = getOne(queryWrapper);

        if (Objects.nonNull(oneHouse)) {
            house.setId(oneHouse.getId());
            updateById(house);
        } else {
            save(house);
        }
    }

    public List<House> getHousesByModeRefId(Long modeRefId, Integer leaseMode) {
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(House::getModeRefId, modeRefId);
        queryWrapper.eq(House::getLeaseMode, leaseMode);
        return list(queryWrapper);
    }

    public Boolean checkHouseExist(Long communityId, String building, String unit, String doorNumber) {
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(House::getCommunityId, communityId);
        queryWrapper.eq(House::getBuilding, building);
        queryWrapper.eq(House::getUnit, unit);
        queryWrapper.eq(House::getDoorNumber, doorNumber);

        return count(queryWrapper) > 0;
    }

    public String getScatterAddress(CommunityDTO community, ScatterHouseDTO houseDTO) {
        return String.format("%s%s%s栋%s-%s室", community.getDistrict(),
                community.getName(),
                houseDTO.getBuilding(),
                CharSequenceUtil.isBlank(houseDTO.getUnit()) ? "" : houseDTO.getUnit() + "单元",
                houseDTO.getDoorNumber());
    }
}
