package com.homi.admin.controller.house;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.dto.house.HouseSimpleVO;
import com.homi.domain.enums.house.OperationModeEnum;
import com.homi.service.house.HouseFocusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RequestMapping("/admin/house/focus")
@RestController
@RequiredArgsConstructor
public class FocusController {
    private final HouseFocusService houseFocusService;

    @PostMapping("/create")
    public ResponseResult<Long> createHouse(@RequestBody FocusCreateDTO houseCreateDto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        houseCreateDto.setCompanyId(currentUser.getCompanyId());

        if (Objects.isNull(houseCreateDto.getExcludeFour())) {
            houseCreateDto.setExcludeFour(false);
        }

        houseCreateDto.setCreateBy(currentUser.getId());
        houseCreateDto.setCreateTime(DateUtil.date());
        houseCreateDto.setUpdateBy(currentUser.getId());
        houseCreateDto.setUpdateTime(DateUtil.date());

        Long houseId;
        if (Objects.nonNull(houseCreateDto.getId())) {
            houseId = houseFocusService.updateHouseFocus(houseCreateDto);
        } else {
            houseCreateDto.setCreateBy(currentUser.getId());
            houseCreateDto.setCreateTime(DateUtil.date());
            houseId = houseFocusService.createHouseFocus(houseCreateDto);
        }

        houseFocusService.updateHouseRoomCount(houseId);

        return ResponseResult.ok(houseId);
    }

    @PostMapping("/house/options")
    public ResponseResult<List<HouseSimpleVO>> houseOptions() {
        return ResponseResult.ok(houseFocusService.getHouseOptionList(OperationModeEnum.FOCUS));
    }

    /**
     * 获取房源详情
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/21 13:28
     *
     * @param id 参数说明
     * @return com.homi.domain.base.ResponseResult<com.homi.domain.dto.house.FocusCreateDTO>
     */
    @GetMapping("/get")
    public ResponseResult<FocusCreateDTO> getById(@RequestParam("id") Long id) {
        return ResponseResult.ok(houseFocusService.getHouseById(id));
    }
}

