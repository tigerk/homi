package com.homi.saas.web.controller.dashboard;

import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dashboard.vo.WelcomeDashboardVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.dashboard.WelcomeDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/dashboard/welcome")
@Tag(name = "欢迎页看板")
public class WelcomeDashboardController {
    private final WelcomeDashboardService welcomeDashboardService;

    @PostMapping("/summary")
    @Operation(summary = "获取欢迎页聚合数据")
    public ResponseResult<WelcomeDashboardVO> getSummary(@AuthenticationPrincipal UserLoginVO loginUser) {
        List<Long> roleIds = loginUser.getRoles() == null ? List.of() : loginUser.getRoles().stream().map(Long::valueOf).toList();
        return ResponseResult.ok(welcomeDashboardService.getSummary(loginUser.getCurCompanyId(), roleIds));
    }
}
