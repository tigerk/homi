package com.homi.service.system;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.company.CompanyQueryDTO;
import com.homi.model.entity.Company;
import com.homi.model.repo.CompanyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepo companyRepo;

    public PageVO<Company> getCompanyList(CompanyQueryDTO query) {
        Page<Company> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(query.getName())) {
            queryWrapper.like(Company::getName, query.getName());
        }

        if (CharSequenceUtil.isNotBlank(query.getContactName())) {
            queryWrapper.like(Company::getContactName, query.getContactName());
        }

        if (CharSequenceUtil.isNotBlank(query.getPhone())) {
            queryWrapper.like(Company::getPhone, query.getPhone());
        }

        if (query.getStatus() != null) {
            queryWrapper.eq(Company::getStatus, query.getStatus());
        }

        IPage<Company> companyPage = companyRepo.page(page, queryWrapper);

        PageVO<Company> pageVO = new PageVO<>();
        pageVO.setTotal(companyPage.getTotal());
        pageVO.setList(companyPage.getRecords());
        pageVO.setCurrentPage(companyPage.getCurrent());
        pageVO.setPageSize(companyPage.getSize());
        pageVO.setPages(companyPage.getPages());

        return pageVO;
    }
}
