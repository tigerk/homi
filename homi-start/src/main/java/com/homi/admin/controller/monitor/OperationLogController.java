package com.homi.admin.controller.monitor;


import com.homi.admin.auth.service.AuthService;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.monitor.OperationLogDTO;
import com.homi.model.entity.SysOperationLog;
import com.homi.model.repo.SysOperationLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/monitor/operation/log")
@RestController
@RequiredArgsConstructor
public class OperationLogController {
    private final SysOperationLogRepo sysOperationLogRepo;

    private final AuthService authService;

    @PostMapping("/list")
    public ResponseResult<PageVO<SysOperationLog>> getLoginLogList(@RequestBody OperationLogDTO dto) {
        return ResponseResult.ok(sysOperationLogRepo.getList(dto));
    }
}

