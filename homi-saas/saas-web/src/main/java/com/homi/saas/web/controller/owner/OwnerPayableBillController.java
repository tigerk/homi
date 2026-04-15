package com.homi.saas.web.controller.owner;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.owner.dto.OwnerPayableBillCancelDTO;
import com.homi.model.owner.dto.OwnerPayableBillCreateDTO;
import com.homi.model.owner.dto.OwnerPayableBillIdDTO;
import com.homi.model.owner.dto.OwnerPayableBillPaymentCreateDTO;
import com.homi.model.owner.dto.OwnerPayableBillQueryDTO;
import com.homi.model.owner.dto.OwnerPayableBillUpdateDTO;
import com.homi.model.owner.vo.OwnerPayableBillDetailVO;
import com.homi.model.owner.vo.OwnerPayableBillListVO;
import com.homi.model.owner.vo.OwnerPayableBillSummaryVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.owner.OwnerPayableBillService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/owner/payable-bill")
public class OwnerPayableBillController {
    private final OwnerPayableBillService ownerPayableBillService;

    @PostMapping("/page")
    @Operation(summary = "包租业主应付单分页列表")
    public ResponseResult<PageVO<OwnerPayableBillListVO>> page(@RequestBody OwnerPayableBillQueryDTO queryDTO) {
        return ResponseResult.ok(ownerPayableBillService.page(queryDTO));
    }

    @PostMapping("/summary")
    @Operation(summary = "包租业主应付单汇总")
    public ResponseResult<OwnerPayableBillSummaryVO> summary(@RequestBody OwnerPayableBillQueryDTO queryDTO) {
        return ResponseResult.ok(ownerPayableBillService.summary(queryDTO));
    }

    @PostMapping("/detail")
    @Operation(summary = "包租业主应付单详情")
    public ResponseResult<OwnerPayableBillDetailVO> detail(@RequestBody OwnerPayableBillIdDTO dto) {
        return ResponseResult.ok(ownerPayableBillService.detail(dto));
    }

    @PostMapping("/create")
    @Operation(summary = "新增包租业主应付单")
    public ResponseResult<Long> create(@RequestBody OwnerPayableBillCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerPayableBillService.create(dto, loginUser.getId(), loginUser.getUsername()));
    }

    @PostMapping("/update")
    @Operation(summary = "修改包租业主应付单")
    public ResponseResult<Long> update(@RequestBody OwnerPayableBillUpdateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerPayableBillService.update(dto, loginUser.getId(), loginUser.getUsername()));
    }

    @PostMapping("/cancel")
    @Operation(summary = "作废包租业主应付单")
    public ResponseResult<Long> cancel(@RequestBody OwnerPayableBillCancelDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerPayableBillService.cancel(dto, loginUser.getId(), loginUser.getUsername()));
    }

    @PostMapping("/payment/create")
    @Operation(summary = "登记包租业主应付单付款")
    public ResponseResult<Long> paymentCreate(@RequestBody OwnerPayableBillPaymentCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerPayableBillService.createPayment(dto, loginUser.getId(), loginUser.getUsername()));
    }
}
