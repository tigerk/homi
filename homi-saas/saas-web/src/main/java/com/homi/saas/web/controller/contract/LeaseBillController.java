package com.homi.saas.web.controller.contract;

import cn.hutool.core.text.CharSequenceUtil;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.tenant.dto.LeaseBillCollectDTO;
import com.homi.model.tenant.dto.LeaseBillDetailDTO;
import com.homi.model.tenant.dto.LeaseBillUpdateDTO;
import com.homi.model.tenant.dto.LeaseBillVoidDTO;
import com.homi.model.tenant.dto.LeaseQueryDTO;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.lease.bill.LeaseBillService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/contract/lease/bill")
public class LeaseBillController {

    private final LeaseBillService leaseBillService;

    @PostMapping("/list")
    @Operation(summary = "根据租客ID查询租客账单列表")
    public ResponseResult<List<LeaseBillListVO>> getBillList(@RequestBody LeaseQueryDTO queryDTO,
                                                             @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(leaseBillService.getBillListByLeaseId(queryDTO.getLeaseId(), Boolean.FALSE));
    }

    @PostMapping("/history/list")
    @Operation(summary = "根据租客ID查询租客历史账单列表")
    public ResponseResult<List<LeaseBillListVO>> getBillHistoryList(@RequestBody LeaseQueryDTO queryDTO,
                                                                    @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(leaseBillService.getBillListByLeaseId(queryDTO.getLeaseId(), Boolean.TRUE));
    }

    @PostMapping("/detail")
    @Operation(summary = "根据账单ID查询账单详情")
    public ResponseResult<LeaseBillListVO> getBillDetail(@RequestBody LeaseBillDetailDTO queryDTO) {
        return ResponseResult.ok(leaseBillService.getBillDetailById(queryDTO.getBillId()));
    }

    @PostMapping("/update")
    @Operation(summary = "更新租客账单")
    public ResponseResult<Boolean> updateBill(@RequestBody LeaseBillUpdateDTO updateDTO,
                                              @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(leaseBillService.updateBill(updateDTO, loginUser.getId()));
    }

    @PostMapping("/collect")
    @Operation(summary = "租客账单收款")
    public ResponseResult<Boolean> collectBill(@RequestBody LeaseBillCollectDTO collectDTO,
                                               @AuthenticationPrincipal UserLoginVO loginUser) {
        if (collectDTO == null || collectDTO.getId() == null) {
            throw new BizException(ResponseCodeEnum.PARAM_ERROR);
        }

        if (collectDTO.getTotalAmount() == null || collectDTO.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ResponseCodeEnum.PARAM_ERROR);
        }
        if (collectDTO.getItems() == null || collectDTO.getItems().isEmpty()) {
            throw new BizException(ResponseCodeEnum.PARAM_ERROR);
        }

        collectDTO.setUpdateBy(loginUser.getId());
        return ResponseResult.ok(leaseBillService.collectBill(collectDTO));
    }

    @PostMapping("/void")
    @Operation(summary = "作废租客账单")
    public ResponseResult<Boolean> voidBill(@RequestBody LeaseBillVoidDTO voidDTO,
                                            @AuthenticationPrincipal UserLoginVO loginUser) {
        if (voidDTO == null || voidDTO.getBillId() == null) {
            throw new BizException(ResponseCodeEnum.PARAM_ERROR);
        }

        if (CharSequenceUtil.isBlank(voidDTO.getVoidReason())) {
            throw new BizException("作废原因不能为空");
        }

        voidDTO.setUpdateBy(loginUser.getId());
        return ResponseResult.ok(leaseBillService.voidBill(voidDTO));
    }
}
