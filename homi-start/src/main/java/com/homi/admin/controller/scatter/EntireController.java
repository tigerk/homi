package com.homi.admin.controller.scatter;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.scatter.EntireCreateDTO;
import com.homi.model.repo.UploadedFileRepo;
import com.homi.service.house.EntireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RequestMapping("/admin/scatter/entire")
@RestController
@RequiredArgsConstructor
public class EntireController {
    private final EntireService entireService;

    private final UploadedFileRepo uploadedFileRepo;

    @PostMapping("/create")
    public ResponseResult<Boolean> createHouse(@RequestBody EntireCreateDTO entireCreateDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        entireCreateDTO.setCompanyId(currentUser.getCurCompanyId());

        entireCreateDTO.setUpdateBy(currentUser.getId());
        entireCreateDTO.setUpdateTime(DateUtil.date());

        Boolean success;
        if (Objects.nonNull(entireCreateDTO.getId())) {
            success = entireService.updateHouseEntire(entireCreateDTO);
        } else {
            entireCreateDTO.setCreateBy(currentUser.getId());
            entireCreateDTO.setCreateTime(DateUtil.date());
            success = entireService.createHouseEntire(entireCreateDTO);
        }

        return ResponseResult.ok(success);
    }
}

