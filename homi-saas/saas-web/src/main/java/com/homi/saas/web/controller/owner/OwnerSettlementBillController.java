package com.homi.saas.web.controller.owner;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.owner.dto.OwnerRealtimeSettlementCompensateDTO;
import com.homi.model.owner.dto.OwnerSettlementBillIdDTO;
import com.homi.model.owner.dto.OwnerSettlementBillQueryDTO;
import com.homi.model.owner.vo.OwnerSettlementBillDetailVO;
import com.homi.model.owner.vo.OwnerSettlementBillListVO;
import com.homi.model.owner.vo.OwnerSettlementBillSummaryVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.owner.OwnerBillingGenerateService;
import com.homi.service.service.owner.OwnerSettlementBillService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/owner/settlement-bill")
public class OwnerSettlementBillController {
    private final OwnerSettlementBillService ownerSettlementBillService;
    private final OwnerBillingGenerateService ownerBillingGenerateService;

    @PostMapping("/page")
    @Operation(summary = "轻托管业主结算单分页列表")
    public ResponseResult<PageVO<OwnerSettlementBillListVO>> page(@RequestBody OwnerSettlementBillQueryDTO queryDTO) {
        return ResponseResult.ok(ownerSettlementBillService.page(queryDTO));
    }

    @PostMapping("/summary")
    @Operation(summary = "轻托管业主结算单汇总")
    public ResponseResult<OwnerSettlementBillSummaryVO> summary(@RequestBody OwnerSettlementBillQueryDTO queryDTO) {
        return ResponseResult.ok(ownerSettlementBillService.summary(queryDTO));
    }

    @PostMapping("/detail")
    @Operation(summary = "轻托管业主结算单详情")
    public ResponseResult<OwnerSettlementBillDetailVO> detail(@RequestBody OwnerSettlementBillIdDTO dto) {
        return ResponseResult.ok(ownerSettlementBillService.detail(dto));
    }

    @PostMapping("/realtime/compensate")
    @Operation(summary = "手动补偿轻托管租客支付实时分账")
    public ResponseResult<Integer> compensateRealtimeSettlement(@RequestBody(required = false) OwnerRealtimeSettlementCompensateDTO dto,
                                                                @AuthenticationPrincipal UserLoginVO loginUser) {
        Long contractId = dto == null ? null : dto.getContractId();
        return ResponseResult.ok(ownerBillingGenerateService.compensateRealtimeSettlementBills(
            loginUser.getCurCompanyId(),
            contractId,
            loginUser.getId()
        ));
    }
}
