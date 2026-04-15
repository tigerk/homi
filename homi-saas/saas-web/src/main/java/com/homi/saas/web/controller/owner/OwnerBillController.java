package com.homi.saas.web.controller.owner;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.owner.dto.OwnerWithdrawApplyIdDTO;
import com.homi.model.owner.dto.OwnerWithdrawApplyQueryDTO;
import com.homi.model.owner.dto.OwnerWithdrawCreateDTO;
import com.homi.model.owner.dto.OwnerWithdrawOperateDTO;
import com.homi.model.owner.vo.OwnerWithdrawApplyDetailVO;
import com.homi.model.owner.vo.OwnerWithdrawApplyListVO;
import com.homi.model.owner.vo.OwnerWithdrawSummaryVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.owner.OwnerBillService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/owner")
public class OwnerBillController {
    private final OwnerBillService ownerBillService;

    @PostMapping("/withdraw/page")
    @Operation(summary = "业主提现申请分页列表")
    public ResponseResult<PageVO<OwnerWithdrawApplyListVO>> withdrawPage(@RequestBody OwnerWithdrawApplyQueryDTO queryDTO) {
        return ResponseResult.ok(ownerBillService.pageOwnerWithdrawApplies(queryDTO));
    }

    @PostMapping("/withdraw/summary")
    @Operation(summary = "业主提现汇总")
    public ResponseResult<OwnerWithdrawSummaryVO> withdrawSummary(@RequestBody OwnerWithdrawApplyQueryDTO queryDTO) {
        return ResponseResult.ok(ownerBillService.summaryOwnerWithdrawApplies(queryDTO));
    }

    @PostMapping("/withdraw/detail")
    @Operation(summary = "业主提现申请详情")
    public ResponseResult<OwnerWithdrawApplyDetailVO> withdrawDetail(@RequestBody OwnerWithdrawApplyIdDTO dto) {
        return ResponseResult.ok(ownerBillService.getOwnerWithdrawApplyDetail(dto));
    }

    @PostMapping("/withdraw/create")
    @Operation(summary = "发起业主提现")
    public ResponseResult<Long> withdrawCreate(@RequestBody OwnerWithdrawCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerBillService.createOwnerWithdrawApply(dto, loginUser.getId()));
    }

    @PostMapping("/withdraw/operate")
    @Operation(summary = "业主提现状态流转")
    public ResponseResult<Long> withdrawOperate(@RequestBody OwnerWithdrawOperateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerBillService.operateOwnerWithdrawApply(dto, loginUser.getId()));
    }
}
