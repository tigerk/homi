package com.homi.admin.controller.sys;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.Log;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.dept.DeptCreateDTO;
import com.homi.domain.dto.dept.DeptQueryDTO;
import com.homi.domain.dto.dept.DeptVO;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.domain.vo.user.UserVO;
import com.homi.service.system.DeptService;
import com.homi.service.system.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RequestMapping("/admin/sys/dept")
@RestController
@RequiredArgsConstructor
@Tag(name = "部门管理")
public class DeptController {
    private final DeptService deptService;

    private final UserService userService;

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

    @GetMapping("/user/list")
    @Operation(summary = "获取部门的用户列表")
    public ResponseResult<List<UserVO>> getDeptUserList(Long deptId) {
        return ResponseResult.ok(userService.getUserListByDeptId(deptId));
    }
}

