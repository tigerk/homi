package com.homi.saas.web.controller.scatter;


import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.house.dto.HouseIdDTO;
import com.homi.model.house.vo.HouseDetailVO;
import com.homi.model.scatter.ScatterCreateDTO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.house.HouseService;
import com.homi.service.service.house.scatter.ScatterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/scatter")
public class ScatterController {
    private final ScatterService scatterService;
    private final HouseService houseService;

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

    @PostMapping("/house/detail")
    public ResponseResult<HouseDetailVO> getScatterHouseDetail(@RequestBody HouseIdDTO houseIdDTO) {
        return ResponseResult.ok(houseService.getHouseDetailById(houseIdDTO.getId()));
    }
}

