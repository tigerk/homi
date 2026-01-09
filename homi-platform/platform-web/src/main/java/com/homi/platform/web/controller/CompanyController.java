package com.homi.platform.web.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.company.dto.CompanyCreateDTO;
import com.homi.model.company.dto.CompanyDeleteDTO;
import com.homi.model.company.dto.CompanyQueryDTO;
import com.homi.model.company.vo.CompanyCreateVO;
import com.homi.model.company.vo.CompanyListVO;
import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
import com.homi.service.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RequestMapping("/platform/company")
@RestController
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping("/list")
    public ResponseResult<PageVO<CompanyListVO>> list(@RequestBody CompanyQueryDTO queryDTO) {
        return ResponseResult.ok(companyService.getCompanyList(queryDTO));
    }

    @PostMapping("/create")
    @SaCheckPermission("platform:company:createOrUpdate")
    public ResponseResult<CompanyCreateVO> createCompany(@RequestBody CompanyCreateDTO createDTO) {
        PlatformUserLoginVO currentUser = PlatformLoginManager.getCurrentUser();
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

    @PostMapping("/status/change")
    @SaCheckPermission("platform:company:createOrUpdate")
    public ResponseResult<Boolean> changeStatus(@RequestBody CompanyCreateDTO createDTO) {
        PlatformUserLoginVO currentUser = PlatformLoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        companyService.changeStatus(createDTO);

        return ResponseResult.ok(Boolean.TRUE);
    }

    @PostMapping("delete")
    public ResponseResult<Boolean> deleteCompany(@RequestBody CompanyDeleteDTO deleteDTO) {
        companyService.deleteCompany(deleteDTO);

        return ResponseResult.ok(Boolean.TRUE);
    }
}

