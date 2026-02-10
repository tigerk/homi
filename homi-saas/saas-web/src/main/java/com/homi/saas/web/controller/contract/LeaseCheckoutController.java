package com.homi.saas.web.controller.contract;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.checkout.dto.LeaseCheckoutDTO;
import com.homi.model.checkout.dto.LeaseCheckoutQueryDTO;
import com.homi.model.checkout.vo.LeaseCheckoutInitVO;
import com.homi.model.checkout.vo.LeaseCheckoutVO;
import com.homi.model.common.dto.OperatorDTO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.checkout.LeaseCheckoutService;
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
public class LeaseCheckoutController {

    private final LeaseCheckoutService leaseCheckoutService;

    /**
     * 获取退租初始化数据（合同信息 + 未付账单 + 预填费用）
     */
    @PostMapping("/init")
    public ResponseResult<LeaseCheckoutInitVO> getCheckoutInitData(
        @RequestBody LeaseCheckoutQueryDTO query) {
        LeaseCheckoutInitVO data = leaseCheckoutService.getCheckoutInitData(query);
        return ResponseResult.ok(data);
    }

    /**
     * 保存退租单（新建/修改，退租并结账）
     */
    @PostMapping("/save")
    public ResponseResult<Long> saveCheckout(@RequestBody @Validated LeaseCheckoutDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        dto.setCompanyId(loginUser.getCurCompanyId());
        dto.setOperatorId(loginUser.getId());
        Long checkoutId = leaseCheckoutService.saveCheckout(dto);
        return ResponseResult.ok(checkoutId);
    }

    /**
     * 提交退租审批（确定）
     */
    @PostMapping("/submit")
    public ResponseResult<Void> submitCheckout(@RequestBody LeaseCheckoutQueryDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        leaseCheckoutService.submitCheckout(query.getCheckoutId(), OperatorDTO.builder().operatorId(loginUser.getId()).operatorName(loginUser.getNickname()).build());
        return ResponseResult.ok();
    }

    /**
     * 取消退租单
     */
    @PostMapping("/cancel")
    public ResponseResult<Void> cancelCheckout(
        @RequestBody LeaseCheckoutQueryDTO query,
        @AuthenticationPrincipal UserLoginVO loginUser) {
        leaseCheckoutService.cancelCheckout(
            query.getCheckoutId(),
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
    public ResponseResult<LeaseCheckoutVO> getCheckoutDetail(
        @RequestBody LeaseCheckoutQueryDTO query) {
        LeaseCheckoutVO vo = leaseCheckoutService.getCheckoutDetail(query.getCheckoutId());
        return ResponseResult.ok(vo);
    }

    /**
     * 根据租客ID获取退租单
     */
    @PostMapping("/getByTenant")
    public ResponseResult<LeaseCheckoutVO> getCheckoutByTenantId(
        @RequestBody LeaseCheckoutQueryDTO query) {
        LeaseCheckoutVO vo = query.getLeaseId() != null
            ? leaseCheckoutService.getCheckoutByLeaseId(query.getLeaseId())
            : leaseCheckoutService.getCheckoutByTenantId(query.getTenantId());
        return ResponseResult.ok(vo);
    }

    /**
     * 查询退租单列表
     */
    @PostMapping("/list")
    public ResponseResult<PageVO<LeaseCheckoutVO>> queryCheckoutList(@RequestBody LeaseCheckoutQueryDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        query.setCompanyId(loginUser.getCurCompanyId());
        PageVO<LeaseCheckoutVO> page = leaseCheckoutService.queryCheckoutList(query);
        return ResponseResult.ok(page);
    }
}
