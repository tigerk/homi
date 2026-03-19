package com.homi.saas.web.controller.finance;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.finance.dto.LeaseBillFinanceQueryDTO;
import com.homi.model.finance.vo.LeaseBillFeeFinanceItemVO;
import com.homi.model.finance.vo.LeaseBillFinanceItemVO;
import com.homi.model.finance.vo.LeaseBillFinanceSummaryVO;
import com.homi.service.service.finance.LeaseBillFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/finance/lease-bill")
public class LeaseBillFinanceController {
    private final LeaseBillFinanceService leaseBillFinanceService;

    @PostMapping("/page")
    @Operation(summary = "租客账单分页列表")
    public ResponseResult<PageVO<LeaseBillFinanceItemVO>> page(@RequestBody LeaseBillFinanceQueryDTO queryDTO) {
        return ResponseResult.ok(leaseBillFinanceService.pageBills(queryDTO));
    }

    @PostMapping("/fee/page")
    @Operation(summary = "租客账单明细分页列表")
    public ResponseResult<PageVO<LeaseBillFeeFinanceItemVO>> feePage(@RequestBody LeaseBillFinanceQueryDTO queryDTO) {
        return ResponseResult.ok(leaseBillFinanceService.pageBillFees(queryDTO));
    }

    @PostMapping("/summary")
    @Operation(summary = "租客账单汇总")
    public ResponseResult<LeaseBillFinanceSummaryVO> summary(@RequestBody LeaseBillFinanceQueryDTO queryDTO) {
        return ResponseResult.ok(leaseBillFinanceService.summary(queryDTO));
    }
}
