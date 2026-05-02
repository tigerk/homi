package com.homi.saas.web.controller.contract;

import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.owner.dto.OwnerCreateDTO;
import com.homi.model.owner.dto.OwnerContractAttachmentUpdateDTO;
import com.homi.model.owner.dto.OwnerContractCheckoutDTO;
import com.homi.model.owner.dto.OwnerContractDocCreateDTO;
import com.homi.model.owner.dto.OwnerContractDocIdDTO;
import com.homi.model.owner.dto.OwnerContractDocVoidDTO;
import com.homi.model.owner.dto.OwnerContractGenerateDTO;
import com.homi.model.owner.dto.OwnerContractIdDTO;
import com.homi.model.owner.dto.OwnerContractOfflineSignDTO;
import com.homi.model.owner.dto.OwnerContractSignStatusUpdateDTO;
import com.homi.model.owner.dto.OwnerContractVoidDTO;
import com.homi.model.owner.dto.OwnerQueryDTO;
import com.homi.model.owner.dto.OwnerRenewDTO;
import com.homi.model.owner.dto.OwnerUpdateDTO;
import com.homi.model.owner.vo.OwnerContractTotalVO;
import com.homi.model.owner.vo.OwnerContractCheckoutInitVO;
import com.homi.model.owner.vo.OwnerDetailVO;
import com.homi.model.owner.vo.OwnerListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.owner.OwnerContractCheckoutService;
import com.homi.service.service.owner.OwnerContractCommandService;
import com.homi.service.service.owner.OwnerContractQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/contract/owner")
public class OwnerContractController {
    private final OwnerContractCommandService ownerContractCommandService;
    private final OwnerContractQueryService ownerContractQueryService;
    private final OwnerContractCheckoutService ownerContractCheckoutService;

    @PostMapping("/create")
    @Log(title = "创建业主合同", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> create(@RequestBody OwnerCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        dto.setCreateBy(loginUser.getId());
        if (dto.getOwnerContract() != null) {
            dto.getOwnerContract().setCompanyId(loginUser.getCurCompanyId());
        }
        return ResponseResult.ok(ownerContractCommandService.createOwnerContract(dto));
    }

    @PostMapping("/renew")
    @Log(title = "业主合同续约", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> renew(@RequestBody OwnerRenewDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        dto.setCreateBy(loginUser.getId());
        if (dto.getOwnerContract() != null) {
            dto.getOwnerContract().setCompanyId(loginUser.getCurCompanyId());
        }
        return ResponseResult.ok(ownerContractCommandService.renewOwnerContract(dto));
    }

    @PostMapping("/checkout")
    @Log(title = "业主合同退房", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> checkout(@RequestBody OwnerContractCheckoutDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCheckoutService.checkoutOwnerContract(dto, loginUser.getId(), loginUser.getNickname()));
    }

    @PostMapping("/checkout/init")
    public ResponseResult<OwnerContractCheckoutInitVO> checkoutInit(@RequestBody OwnerContractIdDTO dto) {
        return ResponseResult.ok(ownerContractCheckoutService.getOwnerContractCheckoutInit(dto));
    }

    @PostMapping("/list")
    public ResponseResult<PageVO<OwnerListVO>> list(@RequestBody OwnerQueryDTO query) {
        return ResponseResult.ok(ownerContractQueryService.getOwnerContractList(query));
    }

    @PostMapping("/total")
    public ResponseResult<OwnerContractTotalVO> total(@RequestBody OwnerQueryDTO query) {
        return ResponseResult.ok(ownerContractQueryService.getOwnerContractTotal(query));
    }

    @PostMapping("/detail")
    public ResponseResult<OwnerDetailVO> detail(@RequestBody OwnerContractIdDTO dto) {
        return ResponseResult.ok(ownerContractQueryService.getOwnerContractDetail(dto));
    }

    @PostMapping("/preview")
    public ResponseEntity<byte[]> preview(@RequestBody OwnerContractDocIdDTO dto) {
        byte[] pdfBytes = ownerContractQueryService.previewOwnerContract(dto);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("owner-preview-" + dto.getOwnerContractDocId() + ".pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/update")
    @Log(title = "编辑业主合同", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> update(@RequestBody OwnerUpdateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        dto.setUpdateBy(loginUser.getId());
        if (dto.getOwnerContract() != null) {
            dto.getOwnerContract().setCompanyId(loginUser.getCurCompanyId());
        }
        return ResponseResult.ok(ownerContractCommandService.updateOwnerContract(dto));
    }

    @PostMapping("/void")
    @Log(title = "作废业主合同", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> voidContract(@RequestBody OwnerContractVoidDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.voidOwnerContract(dto, loginUser.getId()));
    }

    @PostMapping("/contract/attachments/update")
    @Log(title = "更新业主合同资料", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> updateContractAttachments(@RequestBody OwnerContractAttachmentUpdateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.updateOwnerContractAttachments(dto, loginUser.getId()));
    }

    @PostMapping("/contract/generate")
    @Log(title = "重新生成业主合同", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> generateContract(@RequestBody OwnerContractGenerateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.generateOwnerContract(dto, loginUser.getId()));
    }

    @PostMapping("/contract/doc/create")
    @Log(title = "新增业主签约合同", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> createContractDoc(@RequestBody OwnerContractDocCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.createOwnerContractDoc(dto, loginUser.getId()));
    }

    @PostMapping("/contract/sign/status/update")
    @Log(title = "更新业主合同签约状态", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> updateContractSignStatus(@RequestBody OwnerContractSignStatusUpdateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.updateOwnerContractSignStatus(dto, loginUser.getId()));
    }

    @PostMapping("/contract/offline-sign")
    @Log(title = "业主合同线下签约", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> offlineSignContract(@RequestBody OwnerContractOfflineSignDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.offlineSignOwnerContract(dto, loginUser.getId()));
    }

    @PostMapping("/contract/doc/void")
    @Log(title = "作废业主签约合同", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> voidContractDoc(@RequestBody OwnerContractDocVoidDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.voidOwnerContractDoc(dto, loginUser.getId()));
    }

    @PostMapping("/contract/doc/restore")
    @Log(title = "还原业主签约合同", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> restoreContractDoc(@RequestBody OwnerContractDocIdDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractCommandService.restoreOwnerContractDoc(dto, loginUser.getId()));
    }
}
