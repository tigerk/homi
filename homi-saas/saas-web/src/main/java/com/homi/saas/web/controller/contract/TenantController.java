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
import com.homi.model.contract.vo.LeaseContractVO;
import com.homi.model.tenant.dto.LeaseContractGenerateDTO;
import com.homi.model.tenant.dto.TenantCreateDTO;
import com.homi.model.tenant.dto.TenantQueryDTO;
import com.homi.model.tenant.vo.*;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.tenant.LeaseBillService;
import com.homi.service.service.tenant.LeaseContractService;
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
    private final LeaseBillService tenantBillService;
    private final LeaseContractService leaseContractService;

    @PostMapping("/create")
    @Log(title = "创建租客", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> createTenant(@RequestBody TenantCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCreateBy(loginUser.getId());
        if (createDTO.getLease() != null) {
            createDTO.getLease().setCompanyId(loginUser.getCurCompanyId());
        }

        return ResponseResult.ok(tenantService.saveTenantOrFromBooking(createDTO));
    }

    @PostMapping("/renew")
    @Log(title = "租客续签", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> renewLease(@RequestBody TenantCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCreateBy(loginUser.getId());
        if (createDTO.getLease() != null) {
            createDTO.getLease().setCompanyId(loginUser.getCurCompanyId());
        }
        return ResponseResult.ok(tenantService.saveTenantOrFromBooking(createDTO));
    }

    @PostMapping("/update")
    @Log(title = "修改租客", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> updateTenant(@RequestBody TenantCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        createDTO.setCreateBy(loginUser.getId());
        if (createDTO.getLease() != null) {
            createDTO.getLease().setCompanyId(loginUser.getCurCompanyId());
        }

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
    public ResponseResult<PageVO<LeaseListVO>> getTenantList(@RequestBody TenantQueryDTO query) {
        return ResponseResult.ok(tenantService.getTenantList(query));
    }

    @PostMapping("/detail")
    @Schema(description = "根据租约ID查询租约详情，不包含租客账单其他费用")
    public ResponseResult<LeaseDetailVO> getTenantDetail(@RequestBody TenantQueryDTO query) {
        return ResponseResult.ok(tenantService.getLeaseDetailById(query.getLeaseId()));
    }

    @PostMapping("/bill/list")
    @Operation(summary = "根据租客ID查询租客账单列表")
    public ResponseResult<List<LeaseBillListVO>> getBillList(@RequestBody TenantQueryDTO queryDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(tenantBillService.getBillListByLeaseId(queryDTO.getLeaseId(), Boolean.TRUE));
    }

    /**
     * 根据租客ID查询租客无效账单列表
     *
     * @param queryDTO  查询参数
     * @param loginUser 登录用户
     * @return 历史账单列表VO
     */
    @PostMapping("/bill/invalid/list")
    @Operation(summary = "根据租客ID查询租客无效账单列表")
    public ResponseResult<List<LeaseBillListVO>> getBillInvalidList(@RequestBody TenantQueryDTO queryDTO, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(tenantBillService.getBillListByLeaseId(queryDTO.getLeaseId(), Boolean.FALSE));
    }

    @PostMapping(value = "/contract/download")
    @Log(title = "下载租客合同", operationType = OperationTypeEnum.INSERT)
    public ResponseEntity<byte[]> download(@RequestBody TenantQueryDTO query) {
        byte[] pdfBytes = tenantService.downloadContract(query.getLeaseId());

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
    public ResponseResult<LeaseContractVO> generate(@RequestBody LeaseContractGenerateDTO query) {
        LeaseDetailVO leaseDetailVO = tenantService.getLeaseDetailById(query.getLeaseId());
        if (leaseDetailVO == null) {
            throw new IllegalArgumentException("Lease not found");
        }

        query.setLeaseDetailVO(leaseDetailVO);

        return ResponseResult.ok(leaseContractService.generateLeaseContract(query));
    }

    @PostMapping(value = "/contract/sign/status/update")
    @Log(title = "更新租客合同签约状态", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Boolean> updateSignStatus(@RequestBody LeaseContractSignStatusUpdateDTO query) {
        Boolean result = leaseContractService.updateLeaseContractSignStatus(query);

        return ResponseResult.ok(result);
    }

    // 删除租客合同
    @PostMapping(value = "/contract/delete")
    @Log(title = "删除租客合同", operationType = OperationTypeEnum.INSERT)
    @SaCheckPermission("tenant:contract:delete:forbidden")
    public ResponseResult<Boolean> deleteContract(@RequestBody LeaseContractDeleteDTO query) {
        Boolean result = leaseContractService.deleteLeaseContract(query.getLeaseContractId());

        return ResponseResult.ok(result);
    }

    @PostMapping(value = "/cancel")
    @Log(title = "租客作废", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Integer> cancelTenant(@RequestBody TenantQueryDTO query) {

        return ResponseResult.ok(leaseContractService.cancelLease(query.getLeaseId()));
    }

    /**
     * 租客合同预览功能
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 17:32
     */
    @PostMapping("/contract/preview")
    public ResponseEntity<byte[]> previewLeaseContract(@RequestBody TenantQueryDTO query) {
        LeaseContractVO leaseContractVO = leaseContractService.getContractByLeaseId(query.getLeaseId());
        if (leaseContractVO == null) {
            throw new IllegalArgumentException("Tenant Contract not found");
        }

        byte[] pdfBytes = ConvertHtml2PdfUtils.generatePdf(leaseContractVO.getContractContent());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String fileName = "tenant-preview " + query.getLeaseId() + DateUtil.date().toTimestamp() + ".pdf";

        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
