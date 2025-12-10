package com.homi.saas.web.controller.sys;


import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.annotation.Log;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dto.dept.DeptCreateDTO;
import com.homi.model.dto.dept.DeptQueryDTO;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.model.vo.dept.DeptVO;
import com.homi.model.vo.company.user.UserVO;
import com.homi.common.lib.exception.BizException;
import com.homi.service.service.company.CompanyUserService;
import com.homi.service.service.system.DeptService;
import com.homi.service.service.system.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RequestMapping("/saas/sys/dept")
@RestController
@RequiredArgsConstructor
@Tag(name = "部门管理")
public class DeptController {
    private final DeptService deptService;

    private final UserService userService;

    private final CompanyUserService companyUserService;

    @PostMapping("list")
    @Operation(summary = "获取部门列表")
    public ResponseResult<List<DeptVO>> list() {
        DeptQueryDTO queryDTO = new DeptQueryDTO();
        return ResponseResult.ok(deptService.list(queryDTO));
    }

    @PostMapping("/create")
    @Log(title = "部门管理", operationType = OperationTypeEnum.INSERT)
    @Operation(summary = "创建部门")
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

    @PostMapping("/delete")
    @Log(title = "部门管理", operationType = OperationTypeEnum.DELETE)
    @Operation(summary = "删除部门")
    public ResponseResult<Boolean> deleteDept(@RequestBody DeptCreateDTO createDTO) {
        return ResponseResult.ok(deptService.deleteDept(createDTO.getId()));
    }

    @PostMapping("/user/list")
    @Operation(summary = "获取部门的用户列表")
    public ResponseResult<List<UserVO>> getDeptUserList(@RequestBody DeptQueryDTO query) {
        if (Objects.isNull(query.getDeptId())) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR);
        }

        return ResponseResult.ok(companyUserService.getUserListByDeptId(query.getDeptId()));
    }
}

