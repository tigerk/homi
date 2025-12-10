package com.homi.domain.saas.facade.impl;

import com.homi.domain.saas.facade.CompanyMenuFacade;
import com.homi.domain.saas.service.company.CompanyMenuService;
import com.homi.model.dao.entity.Menu;
import com.homi.model.dto.menu.MenuCreateDTO;
import com.homi.model.dto.menu.MenuQueryDTO;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.model.vo.menu.MenuVO;
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
public class CompanyMenuFacadeImpl implements CompanyMenuFacade {
    private final CompanyMenuService companyMenuService;


    /**
     * 获取菜单列表
     *
     * @param queryDTO 查询参数
     * @return 菜单列表
     */
    @Override
    public List<MenuVO> getMenuList(MenuQueryDTO queryDTO) {
        return companyMenuService.getMenuList(queryDTO);
    }

    /**
     * 获取简单菜单列表
     *
     * @return 简单菜单列表
     */
    @Override
    public List<SimpleMenuVO> listSimpleMenu() {
        return companyMenuService.listSimpleMenu();
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteById(Long menuId) {
        return companyMenuService.deleteById(menuId);
    }

    /**
     * 构建菜单树
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    @Override
    public List<AsyncRoutesVO> buildMenuTree(List<Menu> menuList) {
        return companyMenuService.buildMenuTree(menuList);
    }

    /**
     * 根据ID获取菜单
     *
     * @param id 菜单ID
     * @return 菜单
     */
    @Override
    public Menu getMenuById(Long id) {
        return companyMenuService.getMenuById(id);
    }

    /**
     * 创建菜单
     *
     * @param dto 创建参数
     * @return 是否创建成功
     */
    @Override
    public Boolean createMenu(MenuCreateDTO dto) {
        return companyMenuService.createMenu(dto);
    }

    /**
     * 获取菜单列表
     *
     * @return 菜单列表
     */
    @Override
    public List<Menu> getMenuList() {
        return companyMenuService.getMenuList();
    }

    /**
     * 根据ID列表获取菜单列表
     *
     * @param menuIdList 菜单ID列表
     * @return 菜单列表
     */
    @Override
    public List<Menu> getMenuByIds(List<Long> menuIdList) {
        return companyMenuService.getMenuByIds(menuIdList);
    }
}
