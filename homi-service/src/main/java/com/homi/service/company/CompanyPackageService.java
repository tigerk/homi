package com.homi.service.company;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.company.CompanyPackageCreateDTO;
import com.homi.model.entity.CompanyPackage;
import com.homi.model.repo.CompanyPackageRepo;
import com.homi.utils.BeanCopyUtils;
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
public class CompanyPackageService {
    private final CompanyPackageRepo companyPackageRepo;

    public PageVO<CompanyPackage> getPackageList() {
        Page<CompanyPackage> page = new Page<>(1, 100);

        LambdaQueryWrapper<CompanyPackage> queryWrapper = new LambdaQueryWrapper<>();

        IPage<CompanyPackage> companyPackagePage = companyPackageRepo.page(page, queryWrapper);

        PageVO<CompanyPackage> pageVO = new PageVO<>();
        pageVO.setTotal(companyPackagePage.getTotal());
        pageVO.setList(companyPackagePage.getRecords());
        pageVO.setCurrentPage(companyPackagePage.getCurrent());
        pageVO.setPageSize(companyPackagePage.getSize());
        pageVO.setPages(companyPackagePage.getPages());

        return pageVO;
    }

    public Boolean createCompanyPackage(CompanyPackageCreateDTO createDTO) {
        CompanyPackage companyPackage = BeanCopyUtils.copyBean(createDTO, CompanyPackage.class);

        companyPackageRepo.save(companyPackage);

        return true;
    }
}
