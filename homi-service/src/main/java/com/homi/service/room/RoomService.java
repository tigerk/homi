package com.homi.service.room;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.dto.room.RoomTotalItemDTO;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.model.entity.Room;
import com.homi.model.repo.HouseRepo;
import com.homi.model.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RoomService {
    private final HouseRepo houseRepo;

    private final RoomRepo roomRepo;

    /**
     * 获取房间列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @param query 参数说明
     * @return com.homi.domain.base.ResponseResult<com.homi.domain.dto.room.RoomListVO>
     */
    public PageVO<RoomItemDTO> getRoomList(RoomQueryDTO query) {
        Page<RoomItemDTO> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        IPage<RoomItemDTO> roomPage = roomRepo.getBaseMapper().list(page, query);

        roomPage.getRecords().forEach(this::format);

        // 封装返回结果
        PageVO<RoomItemDTO> pageVO = new PageVO<>();
        pageVO.setTotal(roomPage.getTotal());
        pageVO.setList(roomPage.getRecords());
        pageVO.setCurrentPage(roomPage.getCurrent());
        pageVO.setPageSize(roomPage.getSize());
        pageVO.setPages(roomPage.getPages());

        return pageVO;
    }

    public void format(RoomItemDTO room) {
        RoomStatusEnum roomStatusEnum = EnumUtil.getBy(RoomStatusEnum::getCode, room.getRoomStatus());
        room.setRoomStatusName(roomStatusEnum.getName());
        room.setRoomStatusColor(roomStatusEnum.getColor());
    }

    public List<RoomTotalItemDTO> getRoomStatusTotal(RoomQueryDTO query) {
        Map<Integer, RoomTotalItemDTO> result = new HashMap<>();
        RoomStatusEnum[] values = RoomStatusEnum.values();
        for (RoomStatusEnum roomStatusEnum : values) {
            RoomTotalItemDTO roomTotalItemDTO = new RoomTotalItemDTO();
            roomTotalItemDTO.setRoomStatus(roomStatusEnum.getCode());
            roomTotalItemDTO.setRoomStatusName(roomStatusEnum.getName());
            roomTotalItemDTO.setRoomStatusColor(roomStatusEnum.getColor());
            roomTotalItemDTO.setTotal(0);
            result.put(roomStatusEnum.getCode(), roomTotalItemDTO);
        }

        List<RoomTotalItemDTO> statusTotal = roomRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(roomTotalItemDTO -> {
            RoomTotalItemDTO orDefault = result.getOrDefault(roomTotalItemDTO.getRoomStatus(), roomTotalItemDTO);
            orDefault.setTotal(roomTotalItemDTO.getTotal());
        });

        return result.values().stream().toList();
    }

    public RoomStatusEnum calculateRoomStatus(Room room) {
        if (Boolean.TRUE.equals(room.getLeased())) {
            return RoomStatusEnum.LEASED;
        }
        if (Boolean.TRUE.equals(room.getLocked())) {
            return RoomStatusEnum.LOCKED;
        }
        if (room.getVacancyStartTime() != null && room.getVacancyStartTime().after(DateUtil.date())) {
            return RoomStatusEnum.PREPARING;
        }
        return RoomStatusEnum.AVAILABLE;
    }
}
