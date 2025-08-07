package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.House;
import com.homi.model.entity.Room;
import com.homi.model.mapper.HouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public boolean checkHouseCodeExist(String houseCode) {
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(House::getHouseCode, houseCode);
        return count(queryWrapper) > 0;
    }

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

}
