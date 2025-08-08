package com.homi.service.room;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.dto.room.RoomTotalItemDTO;
import com.homi.domain.enums.RoomStatusEnum;
import com.homi.model.repo.HouseRepo;
import com.homi.model.repo.RoomRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
        List<RoomTotalItemDTO> statusTotal = roomRepo.getBaseMapper().getStatusTotal(query);

        statusTotal.forEach(roomTotalItemDTO -> {
            RoomStatusEnum roomStatusEnum = EnumUtil.getBy(RoomStatusEnum::getCode, roomTotalItemDTO.getRoomStatus());
            roomTotalItemDTO.setRoomStatusName(roomStatusEnum.getName());
            roomTotalItemDTO.setRoomStatusColor(roomStatusEnum.getColor());
        });

        return statusTotal;
    }
}
