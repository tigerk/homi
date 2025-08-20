package com.homi.service.room;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.homi.model.entity.HouseLayout;
import com.homi.model.entity.Room;
import com.homi.model.repo.HouseLayoutRepo;
import com.homi.model.repo.HouseRepo;
import com.homi.model.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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

        houseRepo.list().forEach(house -> {
            LambdaUpdateWrapper<Room> wrapper = new LambdaUpdateWrapper<Room>()
                .eq(Room::getHouseId, house.getId());

            List<String> tags = JSONUtil.toList(house.getTags(), String.class);
            String tagsStr = String.join("|", tags);

            roomRepo.list(wrapper).forEach(room -> {
                HouseLayout houseLayoutRepoById = null;
                if (Objects.nonNull(room.getHouseLayoutId())) {
                    houseLayoutRepoById = houseLayoutRepo.getById(room.getHouseLayoutId());
                }

                String keyword = String.format("%s|%s|%s|%s|%s", house.getHouseCode(),
                    house.getHouseName(),
                    tagsStr,
                    Objects.isNull(houseLayoutRepoById) ? "" : houseLayoutRepoById.getLayoutName(),
                    room.getRoomNumber());
                room.setKeywords(keyword);

                roomRepo.updateById(room);
            });
        });

        return Boolean.TRUE;
    }
}
