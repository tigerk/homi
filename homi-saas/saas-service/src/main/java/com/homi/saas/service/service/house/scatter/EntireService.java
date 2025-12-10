package com.homi.saas.service.service.house.scatter;

import cn.hutool.core.date.DateUtil;
import com.homi.model.dto.room.price.PriceConfigDTO;
import com.homi.common.lib.enums.room.RoomStatusEnum;
import com.homi.model.dao.entity.House;
import com.homi.model.dao.entity.Room;
import com.homi.model.dao.repo.RoomRepo;
import com.homi.saas.service.service.price.PriceConfigService;
import com.homi.saas.service.service.room.RoomSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

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
public class EntireService {
    private final RoomRepo roomRepo;

    private final RoomSearchService roomSearchService;

    private final PriceConfigService priceConfigService;

    /**
     * 创建整租房间
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/23 15:40
     *
     * @param house       房源数据
     * @param price       房间价格
     * @param priceConfig 价格配置
     */
    public void createEntireRoom(House house, BigDecimal price, PriceConfigDTO priceConfig) {
        Room room = new Room();

        BeanUtils.copyProperties(house, room);

        room.setHouseId(house.getId());
        room.setPrice(price);

        RoomStatusEnum roomStatusEnum = roomRepo.calculateRoomStatus(room);
        room.setRoomStatus(roomStatusEnum.getCode());
        room.setKeywords(roomSearchService.generateKeywords(room));
        room.setRoomNumber(house.getDoorNumber());
        Room roomBefore = roomRepo.getRoomByHouseIdAndRoomNumber(house.getId(), house.getDoorNumber());
        if (Objects.nonNull(roomBefore)) {
            room.setId(roomBefore.getId());
            roomRepo.updateById(room);
        } else {
            room.setCreateBy(house.getCreateBy());
            room.setCreateTime(house.getCreateTime());

            room.setVacancyStartTime(DateUtil.date());
            roomRepo.save(room);
        }

        if (Objects.isNull(priceConfig)) {
            priceConfig = new PriceConfigDTO();
            priceConfig.setRoomId(room.getId());
            priceConfig.setPrice(price);
        }

        priceConfig.setRoomId(room.getId());
        priceConfigService.createPriceConfig(priceConfig);
    }
}
