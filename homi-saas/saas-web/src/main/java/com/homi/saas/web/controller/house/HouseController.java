package com.homi.saas.web.controller.house;


import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.house.dto.HouseIdDTO;
import com.homi.model.house.dto.HouseQueryDTO;
import com.homi.model.house.vo.HouseDetailVO;
import com.homi.model.house.vo.HouseListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.house.HouseService;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/list")
    public ResponseResult<PageVO<HouseListVO>> getHouseList(@RequestBody HouseQueryDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        query.setCompanyId(loginUser.getCurCompanyId());
        return ResponseResult.ok(houseService.getAvailableHouseList(query));
    }

    @PostMapping("/detail")
    public ResponseResult<HouseDetailVO> getHouseDetail(@RequestBody HouseIdDTO houseIdDTO) {
        return ResponseResult.ok(houseService.getHouseDetailById(houseIdDTO.getId()));
    }
}
