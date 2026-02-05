package com.homi.saas.web.controller.contract;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.checkout.dto.TenantCheckoutDTO;
import com.homi.model.checkout.dto.TenantCheckoutQueryDTO;
import com.homi.model.checkout.vo.CheckoutInitVO;
import com.homi.model.checkout.vo.TenantCheckoutVO;
import com.homi.model.common.dto.OperatorDTO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.checkout.TenantCheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退租管理 Controller（退租并结账）
 */
@RestController
@RequestMapping("/saas/tenant/checkout")
@RequiredArgsConstructor
public class TenantCheckoutController {

    private final TenantCheckoutService tenantCheckoutService;

    /**
     * 获取退租初始化数据（合同信息 + 未付账单 + 预填费用）
     */
    @PostMapping("/init")
    public ResponseResult<CheckoutInitVO> getCheckoutInitData(
        @RequestBody TenantCheckoutQueryDTO query) {
        CheckoutInitVO data = tenantCheckoutService.getCheckoutInitData(query.getTenantId());
        return ResponseResult.ok(data);
    }

    /**
     * 保存退租单（新建/修改，退租并结账）
     */
    @PostMapping("/save")
    public ResponseResult<Long> saveCheckout(
        @RequestBody @Validated TenantCheckoutDTO dto,
        @AuthenticationPrincipal UserLoginVO loginUser) {
        dto.setCompanyId(loginUser.getCurCompanyId());
        dto.setOperatorId(loginUser.getId());
        Long checkoutId = tenantCheckoutService.saveCheckout(dto);
        return ResponseResult.ok(checkoutId);
    }

    /**
     * 提交退租审批（确定）
     */
    @PostMapping("/submit")
    public ResponseResult<Void> submitCheckout(
        @RequestBody TenantCheckoutQueryDTO query,
        @AuthenticationPrincipal UserLoginVO loginUser) {
        tenantCheckoutService.submitCheckout(
            query.getTenantId(),
            OperatorDTO.builder()
                .operatorId(loginUser.getId())
                .operatorName(loginUser.getNickname())
                .build()
        );
        return ResponseResult.ok();
    }

    /**
     * 取消退租单
     */
    @PostMapping("/cancel")
    public ResponseResult<Void> cancelCheckout(
        @RequestBody TenantCheckoutQueryDTO query,
        @AuthenticationPrincipal UserLoginVO loginUser) {
        tenantCheckoutService.cancelCheckout(
            query.getTenantId(),
            OperatorDTO.builder()
                .operatorId(loginUser.getId())
                .operatorName(loginUser.getNickname())
                .build()
        );
        return ResponseResult.ok();
    }

    /**
     * 获取退租单详情
     */
    @PostMapping("/detail")
    public ResponseResult<TenantCheckoutVO> getCheckoutDetail(
        @RequestBody TenantCheckoutQueryDTO query) {
        TenantCheckoutVO vo = tenantCheckoutService.getCheckoutDetail(query.getTenantId());
        return ResponseResult.ok(vo);
    }

    /**
     * 根据租客ID获取退租单
     */
    @PostMapping("/getByTenant")
    public ResponseResult<TenantCheckoutVO> getCheckoutByTenantId(
        @RequestBody TenantCheckoutQueryDTO query) {
        TenantCheckoutVO vo = tenantCheckoutService.getCheckoutByTenantId(query.getTenantId());
        return ResponseResult.ok(vo);
    }

    /**
     * 查询退租单列表
     */
    @PostMapping("/list")
    public ResponseResult<PageVO<TenantCheckoutVO>> queryCheckoutList(
        @RequestBody TenantCheckoutQueryDTO query,
        @AuthenticationPrincipal UserLoginVO loginUser) {
        query.setCompanyId(loginUser.getCurCompanyId());
        PageVO<TenantCheckoutVO> page = tenantCheckoutService.queryCheckoutList(query);
        return ResponseResult.ok(page);
    }
}
