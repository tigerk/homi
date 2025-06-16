package com.homi.admin.controller;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.company.CompanyPackageCreateDTO;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.model.entity.CompanyPackage;
import com.homi.service.company.CompanyPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/company/package")
@RestController
@RequiredArgsConstructor
public class CompanyPackageController {
    private final CompanyPackageService companyPackageService;

    @PostMapping("/list")
    public ResponseResult<PageVO<CompanyPackage>> list() {
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
}

