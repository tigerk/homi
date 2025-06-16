package com.homi.service.company;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.domain.base.PageVO;
import com.homi.domain.dto.company.CompanyPackageCreateDTO;
import com.homi.domain.vo.company.CompanyPackageVO;
import com.homi.exception.BizException;
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

    public PageVO<CompanyPackageVO> getPackageList() {
        Page<CompanyPackage> page = new Page<>(1, 100);

        LambdaQueryWrapper<CompanyPackage> queryWrapper = new LambdaQueryWrapper<>();

        IPage<CompanyPackage> companyPackagePage = companyPackageRepo.page(page, queryWrapper);


        PageVO<CompanyPackageVO> pageVO = new PageVO<>();
        pageVO.setTotal(companyPackagePage.getTotal());
        // 格式化数据
        pageVO.setList(companyPackagePage.getRecords().stream().map(companyPackage -> {
            CompanyPackageVO companyPackageVO = BeanCopyUtils.copyBean(companyPackage, CompanyPackageVO.class);
            companyPackageVO.setPackageMenus(JSONUtil.toList(companyPackage.getPackageMenus(), Long.class));
            return companyPackageVO;
        }).toList());
        pageVO.setCurrentPage(companyPackagePage.getCurrent());
        pageVO.setPageSize(companyPackagePage.getSize());
        pageVO.setPages(companyPackagePage.getPages());

        return pageVO;
    }

    public Boolean createCompanyPackage(CompanyPackageCreateDTO createDTO) {
        boolean exists = companyPackageRepo.exists(new LambdaQueryWrapper<CompanyPackage>().eq(CompanyPackage::getName, createDTO.getName()));
        if (exists) {
            throw new BizException("套餐名称已存在，不允许重复创建");
        }

        CompanyPackage companyPackage = BeanCopyUtils.copyBean(createDTO, CompanyPackage.class);

        companyPackage.setPackageMenus(JSONUtil.toJsonStr(createDTO.getPackageMenus()));

        companyPackageRepo.save(companyPackage);

        return true;
    }

    public Boolean changeStatus(CompanyPackageCreateDTO createDTO) {
        CompanyPackage companyPackage = companyPackageRepo.getBaseMapper().selectById(createDTO.getId());
        companyPackage.setStatus(createDTO.getStatus());

        companyPackageRepo.getBaseMapper().updateById(companyPackage);

        return true;
    }
}
