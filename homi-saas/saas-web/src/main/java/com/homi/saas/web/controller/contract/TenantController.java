package com.homi.saas.web.controller.contract;

import com.homi.common.lib.vo.PageVO;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dto.tenant.TenantCreateDTO;
import com.homi.model.dto.tenant.TenantQueryDTO;
import com.homi.model.vo.tenant.TenantListVO;
import com.homi.model.vo.tenant.TenantTotalItemVO;
import com.homi.model.vo.tenant.TenantTotalVO;
import com.homi.service.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping("/total")
    public ResponseResult<TenantTotalVO> getTenantTotal(@RequestBody TenantQueryDTO query) {
        List<TenantTotalItemVO> tenantStatusTotal = tenantService.getTenantStatusTotal(query);
        TenantTotalVO tenantTotalVO = new TenantTotalVO();
        tenantTotalVO.setStatusList(tenantStatusTotal);

        return ResponseResult.ok(tenantTotalVO);
    }

    @PostMapping("/list")
    public ResponseResult<PageVO<TenantListVO>> getTenantList(@RequestBody TenantQueryDTO query) {
        return ResponseResult.ok(tenantService.getTenantList(query));
    }

    @PostMapping("/create")
    public ResponseResult<Long> createTenant(@RequestBody TenantCreateDTO createDTO) {
        log.info("createTenant: {}", createDTO);
        return ResponseResult.ok(tenantService.createTenant(createDTO));
    }
}
