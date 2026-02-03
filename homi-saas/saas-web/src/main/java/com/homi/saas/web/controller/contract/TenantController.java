package com.homi.saas.web.controller.contract;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.contract.vo.TenantContractVO;
import com.homi.model.tenant.dto.TenantContractGenerateDTO;
import com.homi.model.tenant.dto.TenantCreateDTO;
import com.homi.model.tenant.dto.TenantQueryDTO;
import com.homi.model.tenant.vo.*;
import com.homi.model.tenant.vo.bill.TenantBillListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.tenant.TenantBillService;
import com.homi.service.service.tenant.TenantContractService;
import com.homi.service.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/saas/contract/tenant")
public class TenantController {
    private final TenantService tenantService;
    private final TenantBillService tenantBillService;
    private final TenantContractService tenantContractService;

    @PostMapping("/create")
    @Log(title = "创建租客", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> createTenant(@RequestBody TenantCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCreateBy(loginUser.getId());
        createDTO.getTenant().setCompanyId(loginUser.getCurCompanyId());

        return ResponseResult.ok(tenantService.saveTenantOrFromBooking(createDTO));
    }

    @PostMapping("/update")
    @Log(title = "修改租客", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> updateTenant(@RequestBody TenantCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCreateBy(loginUser.getId());
        createDTO.getTenant().setCompanyId(loginUser.getCurCompanyId());

        return ResponseResult.ok(tenantService.updateTenant(createDTO));
    }

    @PostMapping("/total")
    public ResponseResult<TenantTotalVO> getTenantTotal(@RequestBody TenantQueryDTO query) {
        List<TenantTotalItemVO> tenantStatusTotal = tenantService.getTenantStatusTotal(query);
        TenantTotalVO tenantTotalVO = new TenantTotalVO();
        tenantTotalVO.setStatusList(tenantStatusTotal);

        return ResponseResult.ok(tenantTotalVO);
    }

    @PostMapping("/list")
    public ResponseResult<PageVO<TenantListVO>> getTenantList(@RequestBody TenantQueryDTO query) {
        return ResponseResult.ok(tenantService.getTenantList(query));
    }

    @PostMapping("/detail")
    @Schema(description = "根据租客ID查询租客详情，不包含租客账单其他费用")
    public ResponseResult<TenantDetailVO> getTenantDetail(@RequestBody TenantQueryDTO query) {
        return ResponseResult.ok(tenantService.getTenantDetailById(query.getTenantId()));
    }

    @PostMapping("/bill/list")
    @Operation(summary = "根据租客ID查询租客账单列表")
    public ResponseResult<List<TenantBillListVO>> getBillList(@RequestBody TenantQueryDTO queryDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(tenantBillService.getBillListByTenantId(queryDTO.getTenantId(), Boolean.TRUE));
    }

    /**
     * 根据租客ID查询租客历史账单列表
     *
     * @param queryDTO  查询参数
     * @param loginUser 登录用户
     * @return 历史账单列表VO
     */
    @PostMapping("/bill/history/list")
    @Operation(summary = "根据租客ID查询租客历史账单列表")
    public ResponseResult<List<TenantBillListVO>> getBillHistoryList(@RequestBody TenantQueryDTO queryDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(tenantBillService.getBillListByTenantId(queryDTO.getTenantId(), Boolean.FALSE));
    }

    @PostMapping(value = "/contract/download")
    @Log(title = "下载租客合同", operationType = OperationTypeEnum.INSERT)
    public ResponseEntity<byte[]> download(@RequestBody TenantQueryDTO query) {
        byte[] pdfBytes = tenantService.downloadContract(query.getTenantId());

        // 保存到本地，检查生成的 pdf 是否准确
        try (OutputStream os = new FileOutputStream("租客合同_" + DateUtil.date().toTimestamp() + ".pdf")) {
            os.write(pdfBytes);
        } catch (IOException e) {
            throw new BizException(ResponseCodeEnum.PDF_GENERATE_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
            ContentDisposition.builder("attachment")
                .filename("租客合同_" + DateUtil.date().toTimestamp() + ".pdf", StandardCharsets.UTF_8)
                .build()
        );

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/contract/generate")
    @Log(title = "生成租客合同", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<TenantContractVO> generate(@RequestBody TenantContractGenerateDTO query) {
        TenantDetailVO tenantDetailVO = tenantService.getTenantDetailById(query.getTenantId());
        if (tenantDetailVO == null) {
            throw new IllegalArgumentException("Tenant not found");
        }

        query.setTenantDetailVO(tenantDetailVO);

        return ResponseResult.ok(tenantContractService.generateTenantContractByTenantId(query));
    }

    @PostMapping(value = "/contract/sign/status/update")
    @Log(title = "更新租客合同签约状态", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Boolean> updateSignStatus(@RequestBody TenantContractSignStatusUpdateDTO query) {
        Boolean result = tenantContractService.updateTenantContractSignStatus(query);

        return ResponseResult.ok(result);
    }

    // 删除租客合同
    @PostMapping(value = "/contract/delete")
    @Log(title = "删除租客合同", operationType = OperationTypeEnum.INSERT)
    @SaCheckPermission("tenant:contract:delete:forbidden")
    public ResponseResult<Boolean> deleteContract(@RequestBody TenantContractDeleteDTO query) {
        Boolean result = tenantContractService.deleteTenantContract(query.getTenantContractId());

        return ResponseResult.ok(result);
    }

    @PostMapping(value = "/cancel")
    @Log(title = "租客作废", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Integer> cancelTenant(@RequestBody TenantQueryDTO query) {

        return ResponseResult.ok(tenantContractService.cancelTenant(query.getTenantId()));
    }

    /**
     * 租客合同预览功能
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 17:32
     */
    @PostMapping("/contract/preview")
    public ResponseEntity<byte[]> previewTenantContract(@RequestBody TenantQueryDTO query) {
        TenantContractVO tenantContractVO = tenantContractService.getTenantContractByTenantId(query.getTenantId());
        if (tenantContractVO == null) {
            throw new IllegalArgumentException("Tenant Contract not found");
        }

        byte[] pdfBytes = ConvertHtml2PdfUtils.generatePdf(tenantContractVO.getContractContent());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String fileName = "tenant-preview " + query.getTenantId() + DateUtil.date().toTimestamp() + ".pdf";

        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
