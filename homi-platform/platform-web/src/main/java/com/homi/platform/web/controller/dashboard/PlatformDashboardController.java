package com.homi.platform.web.controller.dashboard;

import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dashboard.vo.PlatformOverviewVO;
import com.homi.service.service.dashboard.PlatformDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/dashboard")
@RequiredArgsConstructor
public class PlatformDashboardController {
    private final PlatformDashboardService platformDashboardService;

    @GetMapping("/overview")
    public ResponseResult<PlatformOverviewVO> getOverview() {
        return ResponseResult.ok(platformDashboardService.getOverview());
    }
}
