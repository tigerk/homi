package com.homi.saas.web.controller.finance;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.finance.dto.PaymentFlowFinanceQueryDTO;
import com.homi.model.finance.dto.PaymentFlowIdDTO;
import com.homi.model.finance.dto.PaymentFlowVoidDTO;
import com.homi.model.finance.vo.PaymentFlowFinanceItemVO;
import com.homi.model.finance.vo.PaymentFlowFinanceSummaryVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.finance.PaymentFlowFinanceService;
import com.homi.service.service.finance.PaymentFlowService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/finance/payment-flow")
public class PaymentFlowFinanceController {
    private final PaymentFlowFinanceService paymentFlowFinanceService;
    private final PaymentFlowService paymentFlowService;

    @PostMapping("/page")
    @Operation(summary = "租客支付流水分页列表")
    public ResponseResult<PageVO<PaymentFlowFinanceItemVO>> page(@RequestBody PaymentFlowFinanceQueryDTO queryDTO) {
        return ResponseResult.ok(paymentFlowFinanceService.page(queryDTO));
    }

    @PostMapping("/summary")
    @Operation(summary = "租客支付流水汇总")
    public ResponseResult<PaymentFlowFinanceSummaryVO> summary(@RequestBody PaymentFlowFinanceQueryDTO queryDTO) {
        return ResponseResult.ok(paymentFlowFinanceService.summary(queryDTO));
    }

    @PostMapping("/detail")
    @Operation(summary = "租客支付流水详情")
    public ResponseResult<PaymentFlowFinanceItemVO> detail(@Valid @RequestBody PaymentFlowIdDTO query) {
        if (query.getId() == null) {
            return null;
        }
        return ResponseResult.ok(paymentFlowFinanceService.detail(query.getId()));
    }

    @PostMapping("/void")
    @Operation(summary = "作废租客支付流水")
    public ResponseResult<Boolean> voidPaymentFlow(@Valid @RequestBody PaymentFlowVoidDTO dto,
                                                   @AuthenticationPrincipal UserLoginVO loginUser) {
        if (dto.getId() == null) {
            throw new BizException("支付流水ID不能为空");
        }
        paymentFlowService.voidLeaseBillPaymentFlow(dto.getId(), dto.getVoidReason(), loginUser.getId());
        return ResponseResult.ok(Boolean.TRUE);
    }
}
