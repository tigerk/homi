package com.homi.admin.controller.scatter;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.house.scatter.ScatterCreateDTO;
import com.homi.service.house.scatter.ScatterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RequestMapping("/admin/scatter")
@RestController
@RequiredArgsConstructor
public class ScatterController {
    private final ScatterService scatterService;

    @PostMapping("/create")
    public ResponseResult<Boolean> createHouse(@RequestBody ScatterCreateDTO scatterCreateDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        scatterCreateDTO.setCompanyId(currentUser.getCurCompanyId());

        scatterCreateDTO.setUpdateBy(currentUser.getId());
        scatterCreateDTO.setUpdateTime(DateUtil.date());

        Boolean success;
        if (Objects.nonNull(scatterCreateDTO.getId())) {
            success = scatterService.updateHouse(scatterCreateDTO);
        } else {
            scatterCreateDTO.setCreateBy(currentUser.getId());
            scatterCreateDTO.setCreateTime(DateUtil.date());
            success = scatterService.createHouse(scatterCreateDTO);
        }

        return ResponseResult.ok(success);
    }
}

