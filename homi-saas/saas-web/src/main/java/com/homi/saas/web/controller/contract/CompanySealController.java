package com.homi.saas.web.controller.contract;

import cn.hutool.core.util.ObjectUtil;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.company.dto.seal.CompanySealCreateDTO;
import com.homi.model.company.dto.seal.CompanySealQueryDTO;
import com.homi.model.company.vo.seal.CompanySealVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.company.CompanySealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/saas/contract/seal")
public class CompanySealController {
    private final CompanySealService companySealService;

    @PostMapping("/list")
    public ResponseResult<List<CompanySealVO>> list(@RequestBody(required = false) CompanySealQueryDTO queryDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        CompanySealQueryDTO query = ObjectUtil.defaultIfNull(queryDTO, new CompanySealQueryDTO());
        query.setCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(companySealService.list(query));
    }

    @PostMapping("/create")
    @Log(title = "创建电子印章", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> create(@Valid @RequestBody CompanySealCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(companySealService.createOrUpdate(createDTO, currentUser.getCurCompanyId(), currentUser.getId()));
    }
}
