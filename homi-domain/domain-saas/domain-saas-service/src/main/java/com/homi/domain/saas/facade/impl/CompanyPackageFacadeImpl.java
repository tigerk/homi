package com.homi.domain.saas.facade.impl;

import com.homi.common.lib.vo.PageVO;
import com.homi.domain.saas.facade.CompanyPackageFacade;
import com.homi.domain.saas.service.company.CompanyPackageService;
import com.homi.model.dto.company.CompanyPackageCreateDTO;
import com.homi.model.dto.company.CompanyPackageQueryDTO;
import com.homi.model.vo.IdNameVO;
import com.homi.model.vo.company.CompanyPackageVO;
import com.homi.model.vo.menu.SimpleMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/10
 */

@Service
@RequiredArgsConstructor
public class CompanyPackageFacadeImpl implements CompanyPackageFacade {
    private final CompanyPackageService companyPackageService;

    /**
     * 获取公司套餐列表
     *
     * @param queryDTO 查询参数
     * @return 公司套餐列表
     */
    @Override
    public PageVO<CompanyPackageVO> getPackageList(CompanyPackageQueryDTO queryDTO) {
        return companyPackageService.getPackageList(queryDTO);
    }

    /**
     * 创建公司套餐
     *
     * @param createDTO 创建参数
     * @return 是否创建成功
     */
    @Override
    public Boolean createCompanyPackage(CompanyPackageCreateDTO createDTO) {
        return companyPackageService.createCompanyPackage(createDTO);
    }

    /**
     * 更新公司套餐
     *
     * @param createDTO 更新参数
     * @return 是否更新成功
     */
    @Override
    public Boolean updateCompanyPackage(CompanyPackageCreateDTO createDTO) {
        return companyPackageService.updateCompanyPackage(createDTO);
    }

    /**
     * 变更公司套餐状态
     *
     * @param createDTO 变更参数
     * @return 是否变更成功
     */
    @Override
    public Boolean changeStatus(CompanyPackageCreateDTO createDTO) {
        return companyPackageService.changeStatus(createDTO);
    }

    /**
     * 获取公司套餐可配置的菜单ID列表
     *
     * @param id 公司套餐ID
     * @return 菜单ID列表
     */
    @Override
    public List<Long> getMenusById(Long id) {
        return companyPackageService.getMenusById(id);
    }

    /**
     * 获取公司套餐可配置的菜单列表
     *
     * @return 菜单列表
     */
    @Override
    public List<SimpleMenuVO> getMenuList() {
        return companyPackageService.getMenuList();
    }

    /**
     * 保存公司套餐可配置的菜单ID列表
     *
     * @param createDTO 保存参数
     * @return 是否保存成功
     */
    @Override
    public Boolean saveMenus(CompanyPackageCreateDTO createDTO) {
        return companyPackageService.saveMenus(createDTO);
    }

    /**
     * 获取公司套餐可配置的菜单ID列表
     *
     * @return 菜单ID列表
     */
    @Override
    public List<IdNameVO> listSimpleMenu() {
        return List.of();
    }
}
