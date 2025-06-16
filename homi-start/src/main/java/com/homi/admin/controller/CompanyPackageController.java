package com.homi.admin.controller;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.company.CompanyPackageCreateDTO;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.company.CompanyPackageVO;
import com.homi.exception.BizException;
import com.homi.service.company.CompanyPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RequestMapping("/admin/company/package")
@RestController
@RequiredArgsConstructor
public class CompanyPackageController {
    private final CompanyPackageService companyPackageService;

    @PostMapping("/list")
    public ResponseResult<PageVO<CompanyPackageVO>> list() {
        return ResponseResult.ok(companyPackageService.getPackageList());
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> list(@RequestBody CompanyPackageCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setCreateBy(currentUser.getId());
        createDTO.setCreateTime(DateUtil.date());
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());
        createDTO.setStatus(StatusEnum.ACTIVE.getValue());

        return ResponseResult.ok(companyPackageService.createCompanyPackage(createDTO));
    }

    @PostMapping("/status/change")
    public ResponseResult<Boolean> changeStatus(@RequestBody CompanyPackageCreateDTO createDTO) {
        if (Objects.isNull(createDTO.getId())) {
            throw new BizException("id 不能为空");
        }

        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        return ResponseResult.ok(companyPackageService.changeStatus(createDTO));
    }
}

