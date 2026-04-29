package com.homi.saas.web.controller.common;

import com.homi.common.lib.response.ResponseResult;
import com.homi.model.common.dto.BizOperateLogQueryDTO;
import com.homi.model.owner.vo.BizOperateLogVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.bizlog.BizOperateLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/biz/operate-log")
public class BizOperateLogController {
    private final BizOperateLogService bizOperateLogService;

    @PostMapping("/list")
    public ResponseResult<List<BizOperateLogVO>> list(@RequestBody BizOperateLogQueryDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(bizOperateLogService.listByBizOrSource(
            loginUser.getCurCompanyId(),
            query.getBizType(),
            query.getBizId(),
            query.getSourceType(),
            query.getSourceId()
        ));
    }
}
