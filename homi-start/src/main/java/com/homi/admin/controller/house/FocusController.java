package com.homi.admin.controller.house;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.enums.house.LeaseModeEnum;
import com.homi.domain.vo.IdNameVO;
import com.homi.service.house.FocusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RequestMapping("/admin/house/focus")
@RestController
@RequiredArgsConstructor
public class FocusController {
    private final FocusService focusService;

    @PostMapping("/create")
    public ResponseResult<Long> createHouse(@RequestBody FocusCreateDTO focusCreateDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        focusCreateDTO.setCompanyId(currentUser.getCurCompanyId());

        focusCreateDTO.setCreateBy(currentUser.getId());
        focusCreateDTO.setCreateTime(DateUtil.date());
        focusCreateDTO.setUpdateBy(currentUser.getId());
        focusCreateDTO.setUpdateTime(DateUtil.date());

        Long focusId;
        if (Objects.nonNull(focusCreateDTO.getId())) {
            focusId = focusService.updateHouseFocus(focusCreateDTO);
        } else {
            focusCreateDTO.setCreateBy(currentUser.getId());
            focusCreateDTO.setCreateTime(DateUtil.date());
            focusId = focusService.createHouseFocus(focusCreateDTO);
        }

        return ResponseResult.ok(focusId);
    }

    /**
     * 集中式项目选项
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:16
     *
     * @return com.homi.domain.base.ResponseResult<java.util.List<com.homi.domain.vo.IdNameVO>>
     */
    @PostMapping("/options")
    public ResponseResult<List<IdNameVO>> houseOptions() {
        return ResponseResult.ok(focusService.getFocusOptionList());
    }
}

