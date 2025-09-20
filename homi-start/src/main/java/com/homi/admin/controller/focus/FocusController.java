package com.homi.admin.controller.focus;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.house.FocusCreateDTO;
import com.homi.domain.vo.IdNameVO;
import com.homi.service.house.FocusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RequestMapping("/admin/focus")
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
    @GetMapping("/options")
    public ResponseResult<List<IdNameVO>> focusOptions() {
        return ResponseResult.ok(focusService.getFocusOptionList());
    }

    @GetMapping("/code/check")
    public ResponseResult<Boolean> checkFocusCodeExist(@RequestParam("focusCode") String focusCode) {
        return ResponseResult.ok(focusService.checkFocusCodeExist(CharSequenceUtil.trim(focusCode)));
    }
}

