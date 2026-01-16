package com.homi.model.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.model.community.dto.CommunityDTO;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.Room;
import com.homi.model.dao.mapper.HouseMapper;
import com.homi.model.scatter.ScatterHouseDTO;
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

    /**
     * 更新房源的房间数量
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 09:09
     *
     * @param houseId 参数说明
     * @return boolean
     */
    public boolean updateHouseRoomCount(Long houseId) {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getHouseId, houseId);
        long roomCount = roomRepo.count(queryWrapper);

        queryWrapper.eq(Room::getRoomStatus, RoomStatusEnum.AVAILABLE.getCode());
        long restCount = roomRepo.count(queryWrapper);

        House house = new House();
        house.setId(houseId);
        house.setRoomCount((int) roomCount);
        house.setRestRoomCount((int) restCount);

        return updateById(house);
    }

    /**
     * 根据id判断是否存在，存在更新，不存在则插入
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:45
     *
     * @param house 参数说明
     */
    public void saveHouse(House house) {
        if (Objects.nonNull(house.getId())) {
            updateById(house);
        } else {
            save(house);
        }
    }

    /**
     * 根据模式引用id和租赁模式查询房源
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 09:09
     *
     * @param leaseModeId 参数说明
     * @param leaseMode   参数说明
     * @return java.util.List<com.homi.model.entity.House>
     */
    public List<House> getHousesByLeaseModeId(Long leaseModeId, Integer leaseMode) {
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(House::getLeaseModeId, leaseModeId);
        queryWrapper.eq(House::getLeaseMode, leaseMode);
        return list(queryWrapper);
    }

    /**
     * 检查房源是否存在
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 09:09
     *
     * @param exceptId    排除的房源id
     * @param communityId 社区id
     * @param building    楼栋号
     * @param unit        单元号
     * @param doorNumber  门牌号
     * @return boolean
     */
    public Boolean checkHouseExist(Long exceptId, Long communityId, String building, String unit, String doorNumber) {
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(exceptId)) {
            queryWrapper.ne(House::getId, exceptId);
        }
        queryWrapper.eq(House::getCommunityId, communityId);
        queryWrapper.eq(House::getBuilding, building);
        queryWrapper.eq(House::getUnit, unit);
        queryWrapper.eq(House::getDoorNumber, doorNumber);

        return count(queryWrapper) > 0;
    }

    /**
     * 生成房源的详细地址
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 09:09
     *
     * @param community 参数说明
     * @param houseDTO  参数说明
     * @return java.lang.String
     */
    public String getScatterAddress(CommunityDTO community, ScatterHouseDTO houseDTO) {
        return String.format("%s%s%s栋%s-%s室", community.getDistrict(),
            community.getName(),
            houseDTO.getBuilding(),
            CharSequenceUtil.isBlank(houseDTO.getUnit()) ? "" : houseDTO.getUnit() + "单元",
            houseDTO.getDoorNumber());
    }
}
