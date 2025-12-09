package com.homi.service.house.scatter;

import cn.hutool.core.date.DateUtil;
import com.homi.domain.dto.room.RoomDetailDTO;
import com.homi.domain.enums.room.RoomStatusEnum;
import com.homi.dao.entity.House;
import com.homi.dao.entity.Room;
import com.homi.dao.repo.RoomRepo;
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
     * @param roomDetailDTO 参数说明
     */
    public void createShareRoom(House house, RoomDetailDTO roomDetailDTO) {
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
        priceConfigService.createPriceConfig(roomDetailDTO.getPriceConfig());
    }

    public void createShareRoom(House house, List<RoomDetailDTO> roomList) {
        roomList.forEach(roomDetailDTO -> createShareRoom(house, roomDetailDTO));
    }
}
