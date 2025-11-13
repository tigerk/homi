package com.homi.admin.controller.contract;

import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.annotation.Log;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.contract.ContractTemplateCreateDTO;
import com.homi.domain.dto.contract.ContractTemplateDeleteDTO;
import com.homi.domain.dto.contract.ContractTemplateQueryDTO;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.domain.vo.contract.ContractTemplateListDTO;
import com.homi.service.contract.ContractTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("admin/contract/template")
public class TemplateController {
    private final ContractTemplateService contractTemplateService;

    @PostMapping("/list")
    public ResponseResult<PageVO<ContractTemplateListDTO>> getContractTemplateList(@RequestBody ContractTemplateQueryDTO query) {
        return ResponseResult.ok(contractTemplateService.getContractTemplateList(query));
    }

    @PostMapping("/create")
    public ResponseResult<Long> createContractTemplate(@RequestBody ContractTemplateCreateDTO createDTO) {
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

        String fileName = "contract-preview " + query.getContractType() + DateUtil.date().toTimestamp() + ".pdf";

        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteContractTemplate(@RequestBody ContractTemplateDeleteDTO deleteDTO) {
        return ResponseResult.ok(contractTemplateService.deleteContractTemplate(deleteDTO));
    }

    @PostMapping("/status/update")
    @Log(title = "修改合同模板状态", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Boolean> updateContractTemplateStatus(@RequestBody ContractTemplateCreateDTO updateDTO) {
        return ResponseResult.ok(contractTemplateService.updateContractTemplateStatus(updateDTO));
    }
}
