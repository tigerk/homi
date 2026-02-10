package com.homi.saas.web.controller.monitor;

import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.OperationLog;
import com.homi.model.dao.repo.OperationLogRepo;
import com.homi.model.monitor.MineLogDTO;
import com.homi.model.monitor.OperationLogDTO;
import com.homi.saas.web.auth.service.AuthService;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/monitor/operation/log")
@Schema(description = "操作日志")
public class OperationLogController {
    private final OperationLogRepo operationLogRepo;

    @PostMapping("/list")
    public ResponseResult<PageVO<OperationLog>> getOperationList(@RequestBody OperationLogDTO dto) {
        return ResponseResult.ok(operationLogRepo.getList(dto));
    }

    @PostMapping("/mine")
    public ResponseResult<PageVO<OperationLog>> getMyOperationList(@RequestBody MineLogDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        List<String> titles = List.of(
            "登录",
            "微信登录",
            "短信登录",
            "发送登录短信",
            "更新密码",
            "更新个人信息",
            "修改密码",
            "发送原手机号验证码",
            "发送新手机号验证码",
            "发送更换邮箱验证码",
            "更换手机号",
            "更换邮箱"
        );
        return ResponseResult.ok(operationLogRepo.getMineOperationLogs(
            currentUser.getCurCompanyId(),
            currentUser.getId(),
            titles,
            dto.getCurrentPage(),
            dto.getPageSize()
        ));
    }

    @PostMapping("/detail")
    public ResponseResult<OperationLog> getOperationDetail(@RequestBody OperationLogDTO dto) {
        return ResponseResult.ok(operationLogRepo.getDetailById(dto.getId()));
    }

    @Log(title = "操作日志", operationType = OperationTypeEnum.CLEAR)
    @PostMapping("/clear/all")
    public ResponseResult<Boolean> clearAll() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        int deleted = operationLogRepo.clearAllByCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(deleted > 0);
    }

    @PostMapping("/batch/delete")
    @Log(title = "操作日志", operationType = OperationTypeEnum.DELETE)
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        boolean deleted = operationLogRepo.batchDeleteByIds(ids);

        return ResponseResult.ok(deleted);
    }
}
