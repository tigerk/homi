package com.homi.platform.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.trial.application.dto.TrialApplicationHandleDTO;
import com.homi.model.trial.application.dto.TrialApplicationQueryDTO;
import com.homi.model.trial.application.vo.TrialApplicationVO;
import com.homi.service.trial.application.TrialApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/trialApplication")
@RequiredArgsConstructor
public class TrialApplicationController {

    private final TrialApplicationService trialApplicationService;

    @PostMapping("/list")
    public ResponseResult<PageVO<TrialApplicationVO>> list(@RequestBody TrialApplicationQueryDTO dto) {
        return ResponseResult.ok(trialApplicationService.getList(dto));
    }

    @PostMapping("/handle")
    public ResponseResult<Boolean> handle(@Valid @RequestBody TrialApplicationHandleDTO dto) {
        return ResponseResult.ok(trialApplicationService.handle(dto, Long.valueOf(StpUtil.getLoginId().toString())));
    }
}
