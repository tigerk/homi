package com.homi.domain.saas.facade;

import com.homi.model.dao.entity.Menu;
import com.homi.model.dto.menu.MenuCreateDTO;
import com.homi.model.dto.menu.MenuQueryDTO;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.model.vo.menu.MenuVO;
import com.homi.model.vo.menu.SimpleMenuVO;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/10
 */

public interface CompanyMenuFacade {
    /**
     * 获取菜单列表
     *
     * @param queryDTO 查询参数
     * @return 菜单列表
     */
    List<MenuVO> getMenuList(MenuQueryDTO queryDTO);

    /**
     * 获取简单菜单列表
     *
     * @return 简单菜单列表
     */
    List<SimpleMenuVO> listSimpleMenu();

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否删除成功
     */
    Boolean deleteById(Long menuId);

    /**
     * 构建菜单树
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    List<AsyncRoutesVO> buildMenuTree(List<Menu> menuList);

    /**
     * 根据ID获取菜单
     *
     * @param id 菜单ID
     * @return 菜单
     */
    Menu getMenuById(Long id);

    /**
     * 创建菜单
     *
     * @param dto 创建参数
     * @return 是否创建成功
     */
    Boolean createMenu(MenuCreateDTO dto);

    /**
     * 获取菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> getMenuList();

    /**
     * 根据ID列表获取菜单列表
     *
     * @param menuIdList 菜单ID列表
     * @return 菜单列表
     */
    List<Menu> getMenuByIds(List<Long> menuIdList);
}
