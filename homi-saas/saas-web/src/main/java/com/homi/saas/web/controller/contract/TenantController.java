package com.homi.saas.web.controller.contract;

import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dto.tenant.TenantCreateDTO;
import com.homi.model.dto.tenant.TenantQueryDTO;
import com.homi.model.vo.tenant.TenantDetailVO;
import com.homi.model.vo.tenant.TenantListVO;
import com.homi.model.vo.tenant.TenantTotalItemVO;
import com.homi.model.vo.tenant.TenantTotalVO;
import com.homi.model.vo.tenant.bill.TenantBillListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.tenant.TenantBillService;
import com.homi.service.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/saas/contract/tenant")
public class TenantController {
    private final TenantService tenantService;
    private final TenantBillService tenantBillService;


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

    @PostMapping("/detail")
    @Schema(description = "根据租客ID查询租客详情")
    public ResponseResult<TenantDetailVO> getTenantDetail(@RequestBody TenantQueryDTO query) {
        return ResponseResult.ok(tenantService.getTenantDetailById(query.getTenantId()));
    }

    @PostMapping("/create")
    @Log(title = "创建租客", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> createTenant(@RequestBody TenantCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCreateBy(loginUser.getId());
        createDTO.getTenant().setCompanyId(loginUser.getCurCompanyId());

        return ResponseResult.ok(tenantService.createTenant(createDTO));
    }

    @PostMapping("/bill/list")
    public ResponseResult<List<TenantBillListVO>> getBillList(@RequestBody TenantQueryDTO queryDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(tenantBillService.getBillListByTenantId(queryDTO.getTenantId()));
    }
}
