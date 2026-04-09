package com.homi.saas.web.controller.owner;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.owner.dto.OwnerBillIdDTO;
import com.homi.model.owner.dto.OwnerBillPaymentCreateDTO;
import com.homi.model.owner.dto.OwnerBillQueryDTO;
import com.homi.model.owner.dto.OwnerWithdrawApplyIdDTO;
import com.homi.model.owner.dto.OwnerWithdrawApplyQueryDTO;
import com.homi.model.owner.dto.OwnerWithdrawCreateDTO;
import com.homi.model.owner.dto.OwnerWithdrawOperateDTO;
import com.homi.model.owner.vo.OwnerBillDetailVO;
import com.homi.model.owner.vo.OwnerBillListVO;
import com.homi.model.owner.vo.OwnerBillSummaryVO;
import com.homi.model.owner.vo.OwnerWithdrawApplyDetailVO;
import com.homi.model.owner.vo.OwnerWithdrawApplyListVO;
import com.homi.model.owner.vo.OwnerWithdrawSummaryVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.owner.OwnerFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/owner")
public class OwnerBillController {
    private final OwnerFinanceService ownerFinanceService;

    @PostMapping("/bill/page")
    @Operation(summary = "业主账单分页列表")
    public ResponseResult<PageVO<OwnerBillListVO>> billPage(@RequestBody OwnerBillQueryDTO queryDTO) {
        return ResponseResult.ok(ownerFinanceService.pageOwnerBills(queryDTO));
    }

    @PostMapping("/bill/summary")
    @Operation(summary = "业主账单汇总")
    public ResponseResult<OwnerBillSummaryVO> billSummary(@RequestBody OwnerBillQueryDTO queryDTO) {
        return ResponseResult.ok(ownerFinanceService.summaryOwnerBills(queryDTO));
    }

    @PostMapping("/bill/detail")
    @Operation(summary = "业主账单详情")
    public ResponseResult<OwnerBillDetailVO> billDetail(@RequestBody OwnerBillIdDTO dto) {
        return ResponseResult.ok(ownerFinanceService.getOwnerBillDetail(dto));
    }

    @PostMapping("/bill/payment/create")
    @Operation(summary = "登记业主账单付款")
    public ResponseResult<Long> billPaymentCreate(@RequestBody OwnerBillPaymentCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerFinanceService.createOwnerBillPayment(dto, loginUser.getId()));
    }

    @PostMapping("/withdraw/page")
    @Operation(summary = "业主提现申请分页列表")
    public ResponseResult<PageVO<OwnerWithdrawApplyListVO>> withdrawPage(@RequestBody OwnerWithdrawApplyQueryDTO queryDTO) {
        return ResponseResult.ok(ownerFinanceService.pageOwnerWithdrawApplies(queryDTO));
    }

    @PostMapping("/withdraw/summary")
    @Operation(summary = "业主提现汇总")
    public ResponseResult<OwnerWithdrawSummaryVO> withdrawSummary(@RequestBody OwnerWithdrawApplyQueryDTO queryDTO) {
        return ResponseResult.ok(ownerFinanceService.summaryOwnerWithdrawApplies(queryDTO));
    }

    @PostMapping("/withdraw/detail")
    @Operation(summary = "业主提现申请详情")
    public ResponseResult<OwnerWithdrawApplyDetailVO> withdrawDetail(@RequestBody OwnerWithdrawApplyIdDTO dto) {
        return ResponseResult.ok(ownerFinanceService.getOwnerWithdrawApplyDetail(dto));
    }

    @PostMapping("/withdraw/create")
    @Operation(summary = "发起业主提现")
    public ResponseResult<Long> withdrawCreate(@RequestBody OwnerWithdrawCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerFinanceService.createOwnerWithdrawApply(dto, loginUser.getId()));
    }

    @PostMapping("/withdraw/operate")
    @Operation(summary = "业主提现状态流转")
    public ResponseResult<Long> withdrawOperate(@RequestBody OwnerWithdrawOperateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerFinanceService.operateOwnerWithdrawApply(dto, loginUser.getId()));
    }
}
