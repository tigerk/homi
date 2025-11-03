package com.homi.service.room;

import com.homi.model.entity.House;
import com.homi.model.entity.HouseLayout;
import com.homi.model.entity.Room;
import com.homi.model.repo.HouseLayoutRepo;
import com.homi.model.repo.HouseRepo;
import com.homi.model.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomSearchService {
    private final HouseRepo houseRepo;

    private final RoomRepo roomRepo;

    private final HouseLayoutRepo houseLayoutRepo;

    public Boolean resetKeyword() {
        log.info("开始更新房间搜索关键字");
        roomRepo.list().forEach(room -> {
            room.setKeywords(generateKeywords(room));
            roomRepo.updateById(room);
        });

        log.info("结束更新房间搜索关键字");

        return Boolean.TRUE;
    }

    /**
     * 生成房间搜索关键字
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/22 11:19
     *
     * @param room 参数说明
     * @return java.lang.String
     */
    public String generateKeywords(Room room) {
        House house = houseRepo.getById(room.getHouseId());
        HouseLayout houseLayoutRepoById = null;
        if (Objects.nonNull(house.getHouseLayoutId())) {
            houseLayoutRepoById = houseLayoutRepo.getById(house.getHouseLayoutId());
        }

        return String.format("%s|%s|%s|%s",
                house.getHouseCode(),
                house.getHouseName(),
                Objects.isNull(houseLayoutRepoById) ? "" : houseLayoutRepoById.getLayoutName(),
                room.getRoomNumber());
    }
}
