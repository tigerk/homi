package com.homi.admin.controller.room;


import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.room.RoomGridDTO;
import com.homi.domain.dto.room.RoomItemDTO;
import com.homi.domain.dto.room.RoomQueryDTO;
import com.homi.domain.dto.room.RoomTotalItemDTO;
import com.homi.domain.vo.room.RoomTotalVO;
import com.homi.service.room.RoomGridService;
import com.homi.service.room.RoomSearchService;
import com.homi.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/room")
@RestController
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    private final RoomGridService roomGridService;

    private final RoomSearchService roomSearchService;

    @PostMapping("/list")
    public ResponseResult<PageVO<RoomItemDTO>> getRoomList(@RequestBody RoomQueryDTO query) {
        return ResponseResult.ok(roomService.getRoomList(query));
    }

    @PostMapping("/total")
    public ResponseResult<RoomTotalVO> getRoomTotal(@RequestBody RoomQueryDTO query) {
        List<RoomTotalItemDTO> roomStatusTotal = roomService.getRoomStatusTotal(query);
        RoomTotalVO roomTotalVO = new RoomTotalVO();
        roomTotalVO.setStatusList(roomStatusTotal);

        return ResponseResult.ok(roomTotalVO);
    }

    @PostMapping("/reset/keyword")
    public ResponseResult<Boolean> resetKeyword() {
        Boolean result = roomSearchService.resetKeyword();
        return ResponseResult.ok(result);
    }


    @PostMapping("/grid")
    public ResponseResult<RoomGridDTO> getRoomGrid(@RequestBody RoomQueryDTO query) {
        return ResponseResult.ok(roomGridService.getRoomGrid(query));
    }

    /**
     * 获取多个小区的摘要数据
     */
    @PostMapping("/aggregated/summary")
    public Result<List<CommunitySummaryDTO>> getPropertiesSummary(@RequestBody List<Long> communityIds) {
        return ResponseResult.ok(roomGridService.getPropertiesSummary(communityIds));
    }
}

