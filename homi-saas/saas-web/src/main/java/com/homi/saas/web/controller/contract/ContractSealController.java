package com.homi.saas.web.controller.contract;

import cn.hutool.core.util.ObjectUtil;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.contract.dto.seal.ContractSealCreateDTO;
import com.homi.model.contract.dto.seal.ContractSealQueryDTO;
import com.homi.model.contract.vo.seal.ContractSealVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.contract.ContractSealService;
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
public class ContractSealController {
    private final ContractSealService contractSealService;

    @PostMapping("/list")
    public ResponseResult<List<ContractSealVO>> list(@RequestBody(required = false) ContractSealQueryDTO queryDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        ContractSealQueryDTO query = ObjectUtil.defaultIfNull(queryDTO, new ContractSealQueryDTO());
        query.setCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(contractSealService.list(query));
    }

    @PostMapping("/create")
    @Log(title = "创建合同电子印章", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> create(@Valid @RequestBody ContractSealCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setCompanyId(currentUser.getCurCompanyId());

        return ResponseResult.ok(contractSealService.createOrUpdate(createDTO, currentUser.getCurCompanyId(), currentUser.getId()));
    }
}
