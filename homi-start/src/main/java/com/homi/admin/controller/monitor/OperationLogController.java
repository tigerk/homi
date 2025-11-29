package com.homi.admin.controller.monitor;


import com.homi.admin.auth.service.AuthService;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.Log;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.monitor.OperationLogDTO;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.model.entity.OperationLog;
import com.homi.model.repo.OperationLogRepo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/monitor/operation/log")
@RestController
@RequiredArgsConstructor
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

