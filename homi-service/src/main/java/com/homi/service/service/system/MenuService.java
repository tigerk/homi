package com.homi.service.service.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.enums.BooleanEnum;
import com.homi.common.lib.enums.MenuTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.Menu;
import com.homi.model.dao.entity.RoleMenu;
import com.homi.model.dao.mapper.MenuMapper;
import com.homi.model.dao.mapper.RoleMenuMapper;
import com.homi.model.dao.repo.MenuRepo;
import com.homi.model.dto.menu.MenuCreateDTO;
import com.homi.model.dto.menu.MenuQueryDTO;
import com.homi.model.vo.menu.AsyncRoutesMetaVO;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.model.vo.menu.MenuVO;
import com.homi.model.vo.menu.SimpleMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/18
 */
@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuMapper menuMapper;

    private final MenuRepo menuRepo;

    private final RoleMenuMapper roleMenuMapper;

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    public List<MenuVO> getPlatformMenuList(MenuQueryDTO queryDTO) {
        LambdaQueryWrapper<Menu> query = new LambdaQueryWrapper<>();

        if (Objects.nonNull(queryDTO.getVisible())) {
            query.eq(Menu::getVisible, queryDTO.getVisible());
        }

        query.orderByAsc(Menu::getSortOrder);


        return menuRepo.list(query).stream().map(m -> {
            MenuVO menuVO = BeanCopyUtils.copyBean(m, MenuVO.class);
            assert menuVO != null;
            menuVO.setSortOrder(m.getSortOrder());
            return menuVO;
        }).collect(Collectors.toList());
    }

    public List<AsyncRoutesVO> buildMenuTreeByRoles(List<Long> roleIdList) {
        List<Menu> menuList = menuMapper.listRoleMenuByRoles(roleIdList, MenuTypeEnum.getMenuList());
        return buildMenuTree(menuList);
    }

    public List<SimpleMenuVO> listSimpleMenu() {
        MenuQueryDTO menuQueryDTO = new MenuQueryDTO();
        menuQueryDTO.setVisible(BooleanEnum.FALSE.getValue());
        List<MenuVO> menuList = getPlatformMenuList(menuQueryDTO);

        return menuList.stream().map(m -> {
            SimpleMenuVO simpleMenuVO = new SimpleMenuVO();
            BeanUtil.copyProperties(m, simpleMenuVO);
            return simpleMenuVO;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long menuId) {
        Menu menu = menuMapper.selectById(menuId);
        if (Objects.isNull(menu)) {
            throw new BizException("菜单不存在");
        }
        menuMapper.deleteById(menu);
        List<Long> deleteMenuIdList = new ArrayList<>();
        deleteMenuIdList.add(menu.getId());
        List<Menu> childList = menuMapper.selectList(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, menu.getId()));
        if (!childList.isEmpty()) {
            List<Long> childIdList = childList.stream().map(Menu::getId).collect(Collectors.toList());

            menuMapper.deleteBatchIds(childIdList);
            deleteMenuIdList.addAll(childIdList);
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getMenuId, deleteMenuIdList));
        return true;
    }

    /**
     * 根据菜单列表构建菜单树
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    public List<AsyncRoutesVO> buildMenuTree(List<Menu> menuList) {
        List<AsyncRoutesVO> rootNodes = new ArrayList<>();
        for (Menu menu : menuList) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootNodes.add(buildMenuNode(menu, menuList));
            }
        }
        // 对根节点进行排序
        rootNodes.sort(Comparator.comparingInt(o -> o.getMeta().getSortOrder()));
        return rootNodes;
    }

    private AsyncRoutesVO buildMenuNode(Menu menu, List<Menu> menuList) {
        // 前端所需字段
        AsyncRoutesVO node = new AsyncRoutesVO();
        node.setPath(menu.getPath());
        node.setName(menu.getName());
        if (CharSequenceUtil.isNotBlank(menu.getComponent())) {
            node.setComponent(menu.getComponent());
        }

        if (CharSequenceUtil.isNotBlank(menu.getRedirect())) {
            node.setRedirect(menu.getRedirect());
        }

        node.setType(menu.getMenuType());
        // 设置路由元信息
        AsyncRoutesMetaVO meta = new AsyncRoutesMetaVO();
        meta.setTitle(menu.getTitle());
        meta.setIcon(menu.getIcon());
        meta.setSortOrder(menu.getSortOrder());
        meta.setShowLink(menu.getShowLink());
        meta.setShowParent(menu.getShowParent());
        meta.setKeepAlive(menu.getKeepAlive());
        meta.setFrameLoading(menu.getFrameLoading());
        meta.setAuths(Optional.ofNullable(menu.getAuths())
            .map(List::of)
            .orElse(Collections.emptyList()));
        meta.setFrameSrc(menu.getFrameSrc());
        node.setMeta(meta);
        // 递归构建子节点
        List<AsyncRoutesVO> children = new ArrayList<>();
        for (Menu childMenu : menuList) {
            if (childMenu.getParentId() != null && childMenu.getParentId().equals(menu.getId())) {
                children.add(buildMenuNode(childMenu, menuList));
            }
        }
        children.sort(Comparator.comparingInt(o -> o.getMeta().getSortOrder()));
        node.setChildren(children);
        return node;
    }

    /**
     * 根据id获取菜单
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/27 16:28
     *
     * @param id 参数说明
     * @return com.homi.model.entity.Menu
     */
    public Menu getMenuById(Long id) {
        return menuMapper.selectById(id);
    }

    public Boolean createMenu(MenuCreateDTO dto) {
        Menu menu = BeanCopyUtils.copyBean(dto, Menu.class);

        assert menu != null;
        menu.setFrameLoading(dto.getFrameLoading());
        menu.setKeepAlive(dto.getKeepAlive());
        menu.setHiddenTag(dto.getHiddenTag());
        menu.setFixedTag(dto.getFixedTag());
        menu.setShowLink(dto.getShowLink());
        menu.setShowParent(dto.getShowParent());

        if (Objects.isNull(dto.getId())) {
            menuRepo.getBaseMapper().insert(menu);
        } else {
            menuRepo.getBaseMapper().updateById(menu);
        }

        return true;
    }

    public void updateById(Menu menu) {
        menuRepo.updateById(menu);
    }

    /**
     * 根据菜单id列表获取菜单数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/25 14:17
     *
     * @param menuIdList 参数说明
     * @return java.util.List<com.homi.model.entity.Menu>
     */
    public List<Menu> getMenuByIds(List<Long> menuIdList) {
        return menuMapper.selectBatchIds(menuIdList);
    }

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    public List<MenuVO> getMenuList(MenuQueryDTO queryDTO) {
        LambdaQueryWrapper<Menu> query = new LambdaQueryWrapper<>();

        if (Objects.nonNull(queryDTO.getVisible())) {
            query.eq(Menu::getVisible, queryDTO.getVisible());
        }

        query.orderByAsc(Menu::getSortOrder);

        return menuRepo.list(query).stream().map(m -> {
            MenuVO menuVO = BeanCopyUtils.copyBean(m, MenuVO.class);
            assert menuVO != null;
            menuVO.setSortOrder(m.getSortOrder());
            return menuVO;
        }).collect(Collectors.toList());
    }
}
