package com.homi.service.house.scatter;

import cn.hutool.core.date.DateUtil;
import com.homi.domain.dto.room.RoomCreateDTO;
import com.homi.domain.enums.room.RoomStatusEnum;
import com.homi.model.entity.House;
import com.homi.model.entity.Room;
import com.homi.model.repo.RoomRepo;
import com.homi.service.price.PriceConfigService;
import com.homi.service.room.RoomSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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
public class ShareService {
    private final RoomRepo roomRepo;

    private final RoomSearchService roomSearchService;
    private final PriceConfigService priceConfigService;


    /**
     * 设置价格
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/23 15:40
     *
     * @param house         参数说明
     * @param roomCreateDTO 参数说明
     */
    public void createShareRoom(House house, RoomCreateDTO roomCreateDTO) {
        Room room = new Room();

        BeanUtils.copyProperties(roomCreateDTO, room);

        room.setHouseId(house.getId());
        RoomStatusEnum roomStatusEnum = roomRepo.calculateRoomStatus(room);
        room.setRoomStatus(roomStatusEnum.getCode());
        room.setKeywords(roomSearchService.generateKeywords(room));

        Room roomBefore = roomRepo.getRoomByHouseIdAndRoomNumber(house.getId(), roomCreateDTO.getRoomNumber());
        if (Objects.nonNull(roomBefore)) {
            room.setId(roomBefore.getId());
            roomRepo.updateById(room);
        } else {
            room.setCreateBy(house.getCreateBy());
            room.setCreateTime(house.getCreateTime());
            room.setVacancyStartTime(DateUtil.date());
            roomRepo.save(room);
        }

        roomCreateDTO.getPriceConfig().setRoomId(room.getId());
        priceConfigService.createPriceConfig(roomCreateDTO.getPriceConfig());
    }

    public void createShareRoom(House house, List<RoomCreateDTO> roomList) {
        roomList.forEach(roomCreateDTO -> createShareRoom(house, roomCreateDTO));
    }
}
