package com.homi.domain.saas.facade;

import com.homi.common.lib.vo.PageVO;
import com.homi.model.dto.company.CompanyPackageCreateDTO;
import com.homi.model.dto.company.CompanyPackageQueryDTO;
import com.homi.model.vo.IdNameVO;
import com.homi.model.vo.company.CompanyPackageVO;
import com.homi.model.vo.menu.SimpleMenuVO;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/10
 */

public interface CompanyPackageFacade {
    /**
     * 获取公司套餐列表
     *
     * @param queryDTO 查询参数
     * @return 公司套餐列表
     */
    PageVO<CompanyPackageVO> getPackageList(CompanyPackageQueryDTO queryDTO);

     /**
     * 创建公司套餐
     *
     * @param createDTO 创建参数
     * @return 是否创建成功
     */
    Boolean createCompanyPackage(CompanyPackageCreateDTO createDTO);

    /**
     * 更新公司套餐
     *
     * @param createDTO 更新参数
     * @return 是否更新成功
     */
    Boolean updateCompanyPackage(CompanyPackageCreateDTO createDTO);

    /**
     * 变更公司套餐状态
     *
     * @param createDTO 变更参数
     * @return 是否变更成功
     */
    Boolean changeStatus(CompanyPackageCreateDTO createDTO);

    /**
     * 获取公司套餐可配置的菜单ID列表
     *
     * @param id 公司套餐ID
     * @return 菜单ID列表
     */
    List<Long> getMenusById(Long id);

    /**
     * 获取公司套餐可配置的菜单列表
     *
     * @return 菜单列表
     */
    List<SimpleMenuVO> getMenuList();

    /**
     * 保存公司套餐可配置的菜单ID列表
     *
     * @param createDTO 保存参数
     * @return 是否保存成功
     */
    Boolean saveMenus(CompanyPackageCreateDTO createDTO);

    /**
     * 获取公司套餐可配置的菜单ID列表
     *
     * @return 菜单ID列表
     */
    List<IdNameVO> listSimpleMenu();
}
