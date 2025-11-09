package com.homi.admin.controller.contract;

import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.tenant.TenantQueryDTO;
import com.homi.domain.vo.tenant.TenantItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.homi.service.tenant.TenantService;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("admin/contract/tenant")
public class TenantController {
    private final TenantService tenantService;

    @PostMapping("/list")
    public ResponseResult<PageVO<TenantItemVO>> getTenantList(@RequestBody TenantQueryDTO query) {
        return ResponseResult.ok(tenantService.getTenantList(query));
    }
}
