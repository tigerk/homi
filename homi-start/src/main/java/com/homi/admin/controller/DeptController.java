package com.homi.admin.controller;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.Log;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.dept.DeptCreateDTO;
import com.homi.domain.dto.dept.DeptQueryDTO;
import com.homi.domain.dto.dept.DeptVO;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.service.system.DeptService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RequestMapping("/admin/dept")
@RestController
@RequiredArgsConstructor
@Schema(description = "部门管理")
public class DeptController {
    private final DeptService deptService;

    @PostMapping("list")
    public ResponseResult<List<DeptVO>> list(@RequestBody DeptQueryDTO queryDTO) {
        return ResponseResult.ok(deptService.list(queryDTO));
    }

    @PostMapping("/create")
    @Log(title = "部门管理", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Boolean> createDept(@RequestBody DeptCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        if (Objects.nonNull(createDTO.getId())) {
            return ResponseResult.ok(deptService.updateDept(createDTO));
        } else {
            createDTO.setCreateBy(currentUser.getId());
            createDTO.setCreateTime(DateUtil.date());
            return ResponseResult.ok(deptService.createDept(createDTO));
        }
    }
//
//    @PostMapping("/status/change")
//    @SaCheckPermission("platform:company:createOrUpdate")
//    public ResponseResult<Boolean> changeStatus(@RequestBody CompanyCreateDTO createDTO) {
//        UserLoginVO currentUser = LoginManager.getCurrentUser();
//        createDTO.setUpdateBy(currentUser.getId());
//        createDTO.setUpdateTime(DateUtil.date());
//
//        companyService.changeStatus(createDTO);
//
//        return ResponseResult.ok(Boolean.TRUE);
//    }
}

