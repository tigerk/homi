package com.homi.admin.controller;


import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.company.CompanyQueryDTO;
import com.homi.domain.vo.company.CompanyListVO;
import com.homi.model.entity.Company;
import com.homi.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/location")
@RestController
@RequiredArgsConstructor
public class LocationController {
    private final CompanyService companyService;

    @PostMapping("/city/list")
    public ResponseResult<PageVO<CompanyListVO>> list(@RequestBody CompanyQueryDTO queryDTO) {
        return ResponseResult.ok(companyService.getCompanyList(queryDTO));
    }
}

