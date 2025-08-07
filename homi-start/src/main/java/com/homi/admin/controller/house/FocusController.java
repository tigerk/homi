package com.homi.admin.controller.house;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.service.house.HouseFocusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        houseCreateDto.setUpdateBy(currentUser.getId());
        houseCreateDto.setUpdateTime(DateUtil.date());

        if (Objects.nonNull(houseCreateDto.getId())) {
            return ResponseResult.ok(houseFocusService.updateHouseFocus(houseCreateDto));
        } else {
            houseCreateDto.setCreateBy(currentUser.getId());
            houseCreateDto.setCreateTime(DateUtil.date());
            return ResponseResult.ok(houseFocusService.createHouseFocus(houseCreateDto));
        }
    }

}

