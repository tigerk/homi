package com.homi.saas.web.controller.house;


import com.homi.common.lib.response.ResponseResult;
import com.homi.model.house.dto.HouseIdDTO;
import com.homi.model.house.vo.HouseDetailVO;
import com.homi.service.service.house.HouseService;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/house")
public class HouseController {
    private final RoomService roomService;
    private final HouseService houseService;

    @PostMapping("/detail")
    public ResponseResult<HouseDetailVO> getHouseDetail(@RequestBody HouseIdDTO houseIdDTO) {
        return ResponseResult.ok(houseService.getHouseDetailById(houseIdDTO.getId()));
    }
}
