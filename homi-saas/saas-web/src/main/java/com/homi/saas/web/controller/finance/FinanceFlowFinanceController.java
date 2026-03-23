package com.homi.saas.web.controller.finance;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.finance.dto.FinanceFlowFinanceQueryDTO;
import com.homi.model.finance.dto.FinanceFlowIdDTO;
import com.homi.model.finance.vo.FinanceFlowFinanceItemVO;
import com.homi.model.finance.vo.FinanceFlowFinanceSummaryVO;
import com.homi.service.service.finance.FinanceFlowFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/finance/finance-flow")
public class FinanceFlowFinanceController {
    private final FinanceFlowFinanceService financeFlowFinanceService;

    @PostMapping("/page")
    @Operation(summary = "租客财务流水分页列表")
    public ResponseResult<PageVO<FinanceFlowFinanceItemVO>> page(@RequestBody FinanceFlowFinanceQueryDTO queryDTO) {
        return ResponseResult.ok(financeFlowFinanceService.page(queryDTO));
    }

    @PostMapping("/summary")
    @Operation(summary = "租客财务流水汇总")
    public ResponseResult<FinanceFlowFinanceSummaryVO> summary(@RequestBody FinanceFlowFinanceQueryDTO queryDTO) {
        return ResponseResult.ok(financeFlowFinanceService.summary(queryDTO));
    }

    @PostMapping("/detail")
    @Operation(summary = "租客财务流水详情")
    public ResponseResult<FinanceFlowFinanceItemVO> detail(@Valid @RequestBody FinanceFlowIdDTO query) {
        return ResponseResult.ok(financeFlowFinanceService.detail(query.getId()));
    }
}
