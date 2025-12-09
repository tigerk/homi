package com.homi.admin.controller.monitor;


import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.Log;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.monitor.LoginLogDTO;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.dao.entity.LoginLog;
import com.homi.dao.repo.LoginLogRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/monitor/login/log")
@RestController
@RequiredArgsConstructor
@Tag(name = "登录日志")
public class LoginLogController {
    private final LoginLogRepo loginLogRepo;

    private final AuthService authService;

    @PostMapping("/list")
    @Operation(summary = "获取登录日志列表")
    public ResponseResult<PageVO<LoginLog>> getLoginLogList(@RequestBody LoginLogDTO dto) {
        return ResponseResult.ok(loginLogRepo.getLoginLogList(dto));
    }


    @PostMapping("/clear/all")
    @Operation(summary = "清空所有登录日志")
    @Log(title = "登录日志", operationType = OperationTypeEnum.CLEAR)
    public ResponseResult<Boolean> clearAll() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        int deleted = loginLogRepo.clearAllByCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(deleted > 0);
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "批量删除登录日志")
    @Log(title = "登录日志", operationType = OperationTypeEnum.DELETE)
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        int deleted = loginLogRepo.batchDeleteByIds(ids);

        return ResponseResult.ok(deleted > 0);
    }
}

