package com.homi.admin.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.company.CompanyQueryDTO;
import com.homi.model.entity.Company;
import com.homi.service.system.CompanyService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/company")
@RestController
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping("/list")
    public ResponseResult<PageVO<Company>> list(@RequestBody CompanyQueryDTO queryDTO) {
        return ResponseResult.ok(companyService.getCompanyList(queryDTO));
    }
}

