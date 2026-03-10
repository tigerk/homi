package com.homi.saas.web.controller.contract;

import cn.hutool.core.util.ObjectUtil;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.company.dto.digitalSign.CompanyDigitalSignCreateDTO;
import com.homi.model.company.dto.digitalSign.CompanyDigitalSignQueryDTO;
import com.homi.model.company.vo.digitalSign.CompanyDigitalSignVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.company.CompanyDigitalSignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/saas/contract/digital-sign")
public class DigitalSignController {
    private final CompanyDigitalSignService companyDigitalSignService;

    @PostMapping("/list")
    public ResponseResult<List<CompanyDigitalSignVO>> list(@RequestBody(required = false) CompanyDigitalSignQueryDTO queryDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        CompanyDigitalSignQueryDTO query = ObjectUtil.defaultIfNull(queryDTO, new CompanyDigitalSignQueryDTO());
        query.setCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(companyDigitalSignService.list(query));
    }

    @PostMapping("/create")
    @Log(title = "创建电子签章", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> create(@Valid @RequestBody CompanyDigitalSignCreateDTO createDTO, @AuthenticationPrincipal UserLoginVO currentUser) {
        createDTO.setCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(companyDigitalSignService.createOrUpdate(createDTO, currentUser.getCurCompanyId(), currentUser.getId()));
    }
}
