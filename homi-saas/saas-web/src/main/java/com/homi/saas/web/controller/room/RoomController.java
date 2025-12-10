package com.homi.saas.web.controller.room;


import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.vo.PageVO;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dto.room.RoomQueryDTO;
import com.homi.model.dto.room.grid.RoomGridDTO;
import com.homi.model.vo.room.RoomListVO;
import com.homi.model.vo.room.RoomTotalItemVO;
import com.homi.model.vo.room.RoomTotalVO;
import com.homi.saas.service.service.room.RoomGridService;
import com.homi.saas.service.service.room.RoomSearchService;
import com.homi.saas.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/saas/room")
@RestController
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    private final RoomGridService roomGridService;

    private final RoomSearchService roomSearchService;

    @PostMapping("/list")
    public ResponseResult<PageVO<RoomListVO>> getRoomList(@RequestBody RoomQueryDTO query) {
        Long userId = LoginManager.getCurrentUser().getId();
        query.setCompanyId(userId);

        return ResponseResult.ok(roomService.getRoomList(query));
    }

    @PostMapping("/total")
    public ResponseResult<RoomTotalVO> getRoomTotal(@RequestBody RoomQueryDTO query) {
        List<RoomTotalItemVO> roomStatusTotal = roomService.getRoomStatusTotal(query);
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
}

