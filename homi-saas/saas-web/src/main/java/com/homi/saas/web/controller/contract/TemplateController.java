package com.homi.saas.web.controller.contract;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.homi.common.lib.annotation.Log;
import com.homi.model.contract.vo.ContractTemplateListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.vo.PageVO;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.contract.dto.ContractTemplateCreateDTO;
import com.homi.model.contract.dto.ContractTemplateDeleteDTO;
import com.homi.model.contract.dto.ContractTemplateQueryDTO;
import com.homi.model.contract.dto.ContractTemplateStatusDTO;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.enums.contract.BookingParamsEnum;
import com.homi.common.lib.enums.contract.ContractTypeEnum;
import com.homi.common.lib.enums.contract.LandlordParamsEnum;
import com.homi.common.lib.enums.contract.TenantParamsEnum;
import com.homi.service.service.contract.ContractTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
@RequestMapping("/saas/contract/template")
@Validated
public class TemplateController {
    private final ContractTemplateService contractTemplateService;

    @PostMapping("/list")
    public ResponseResult<PageVO<ContractTemplateListVO>> getContractTemplateList(@RequestBody ContractTemplateQueryDTO query) {
        return ResponseResult.ok(contractTemplateService.getContractTemplateList(query));
    }

    @PostMapping("/create")
    @Log(title = "创建合同模板", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> createContractTemplate(@Valid @RequestBody ContractTemplateCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setCompanyId(currentUser.getCurCompanyId());

        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());
        if (Objects.isNull(createDTO.getId())) {
            createDTO.setCreateBy(currentUser.getId());
            createDTO.setCreateTime(DateUtil.date());

            return ResponseResult.ok(contractTemplateService.createContractTemplate(createDTO));
        } else {
            return ResponseResult.ok(contractTemplateService.updateContractTemplate(createDTO));
        }
    }

    /**
     * 根据合同类型获取合同模板参数
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/13 23:03
     *
     * @param query 参数说明
     * @return com.homi.common.model.response.ResponseResult<java.util.List<cn.hutool.core.lang.Pair<java.lang.String,java.lang.String>>>
     */
    @PostMapping("params")
    public ResponseResult<List<Pair<String, String>>> getTenantParams(@RequestBody ContractTemplateQueryDTO query) {
        if (query.getContractType().equals(ContractTypeEnum.TENANT.getCode())) {
            // 租客
            return ResponseResult.ok(Arrays.stream(TenantParamsEnum.values()).map(p -> new Pair<>(p.getKey(), p.getValue())).toList());
        } else if (query.getContractType().equals(ContractTypeEnum.LANDLORD.getCode())) {
            // 房东
            return ResponseResult.ok(Arrays.stream(LandlordParamsEnum.values()).map(p -> new Pair<>(p.getKey(), p.getValue())).toList());
        } else if (query.getContractType().equals(ContractTypeEnum.BOOKING.getCode())) {
            // 预定
            return ResponseResult.ok(Arrays.stream(BookingParamsEnum.values()).map(p -> new Pair<>(p.getKey(), p.getValue())).toList());
        } else {
            return ResponseResult.fail(ResponseCodeEnum.CONTRACT_TEMPLATE_ERROR);
        }
    }

    /**
     * 合同预览功能
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 17:32
     */
    @PostMapping("/preview")
    public ResponseEntity<byte[]> previewContractTemplate(@RequestBody ContractTemplateQueryDTO query) {
        byte[] pdfBytes = contractTemplateService.previewContractTemplate(query);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String fileName = "tenant-preview " + query.getContractType() + DateUtil.date().toTimestamp() + ".pdf";

        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/delete")
    @Log(title = "删除合同模板", operationType = OperationTypeEnum.DELETE)
    public ResponseResult<Boolean> deleteContractTemplate(@Valid @RequestBody ContractTemplateDeleteDTO deleteDTO) {
        return ResponseResult.ok(contractTemplateService.deleteContractTemplate(deleteDTO));
    }

    @PostMapping("/status/update")
    @Log(title = "修改合同模板状态", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Boolean> updateContractTemplateStatus(@RequestBody ContractTemplateStatusDTO updateDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        updateDTO.setUpdateBy(currentUser.getId());
        updateDTO.setUpdateTime(DateUtil.date());
        return ResponseResult.ok(contractTemplateService.updateContractTemplateStatus(updateDTO));
    }

    @PostMapping("/my/available")
    public ResponseResult<List<ContractTemplateListVO>> getMyAvailableContractTemplates(@RequestBody ContractTemplateQueryDTO query) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        if(currentUser.getIsCompanyAdmin().equals(Boolean.TRUE)) {
            return ResponseResult.ok(contractTemplateService.getAllContractTemplateList(currentUser.getCurCompanyId(), query.getContractType()));
        }

        return ResponseResult.ok(contractTemplateService.getMyAvailableContractTemplates(currentUser.getId(), currentUser.getCurCompanyId(), query.getContractType()));
    }
}
