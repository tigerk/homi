package com.homi.saas.web.controller.scatter;


import cn.hutool.core.date.DateUtil;
import com.homi.model.house.dto.HouseIdDTO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.scatter.ScatterCreateDTO;
import com.homi.model.scatter.ScatterHouseVO;
import com.homi.service.service.house.scatter.ScatterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/scatter")
public class ScatterController {
    private final ScatterService scatterService;

    @PostMapping("/create")
    public ResponseResult<Boolean> createHouse(@RequestBody ScatterCreateDTO scatterCreateDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        scatterCreateDTO.setCompanyId(currentUser.getCurCompanyId());

        scatterCreateDTO.setCreateBy(currentUser.getId());
        scatterCreateDTO.setCreateTime(DateUtil.date());
        scatterCreateDTO.setUpdateBy(currentUser.getId());
        scatterCreateDTO.setUpdateTime(DateUtil.date());

        Boolean success = scatterService.createOrUpdateHouse(scatterCreateDTO);
        return ResponseResult.ok(success);
    }

    @PostMapping("/get")
    public ResponseResult<ScatterHouseVO> getScatterId(@RequestBody HouseIdDTO houseIdDTO) {
        return ResponseResult.ok(scatterService.getScatterId(houseIdDTO.getId()));
    }
}

