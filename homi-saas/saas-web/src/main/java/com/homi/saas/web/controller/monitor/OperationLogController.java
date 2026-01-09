package com.homi.saas.web.controller.monitor;

import com.homi.saas.web.auth.service.AuthService;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.vo.PageVO;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.monitor.OperationLogDTO;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.model.dao.entity.OperationLog;
import com.homi.model.dao.repo.OperationLogRepo;
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

    private final AuthService authService;

    @PostMapping("/list")
    public ResponseResult<PageVO<OperationLog>> getOperationList(@RequestBody OperationLogDTO dto) {
        return ResponseResult.ok(operationLogRepo.getList(dto));
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
        int deleted = operationLogRepo.batchDeleteByIds(ids);

        return ResponseResult.ok(deleted > 0);
    }
}

