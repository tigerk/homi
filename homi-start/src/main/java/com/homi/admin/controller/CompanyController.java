package com.homi.admin.controller;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.company.CompanyCreateDTO;
import com.homi.domain.dto.company.CompanyQueryDTO;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.company.CompanyListVO;
import com.homi.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RequestMapping("/admin/company")
@RestController
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping("/list")
    public ResponseResult<PageVO<CompanyListVO>> list(@RequestBody CompanyQueryDTO queryDTO) {
        return ResponseResult.ok(companyService.getCompanyList(queryDTO));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> createCompany(@RequestBody CompanyCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        if (Objects.nonNull(createDTO.getId())) {
            return ResponseResult.ok(companyService.updateCompany(createDTO));
        } else {
            createDTO.setCreateBy(currentUser.getId());
            createDTO.setCreateTime(DateUtil.date());
            createDTO.setStatus(StatusEnum.ACTIVE.getValue());
            return ResponseResult.ok(companyService.createCompany(createDTO));
        }

    }
}

