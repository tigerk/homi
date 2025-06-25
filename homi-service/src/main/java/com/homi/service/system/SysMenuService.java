package com.homi.service.system;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.menu.MenuCreateDTO;
import com.homi.domain.dto.menu.MenuQueryDTO;
import com.homi.domain.enums.common.BooleanEnum;
import com.homi.domain.vo.menu.AsyncRoutesMetaVO;
import com.homi.domain.vo.menu.AsyncRoutesVO;
import com.homi.domain.vo.menu.MenuVO;
import com.homi.domain.vo.menu.SimpleMenuVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysMenu;
import com.homi.model.entity.SysRoleMenu;
import com.homi.model.mapper.SysMenuMapper;
import com.homi.model.mapper.SysRoleMenuMapper;
import com.homi.model.repo.SysMenuRepo;
import com.homi.utils.BeanCopyUtils;
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
public class SysMenuService {
    private final SysMenuMapper sysMenuMapper;

    private final SysMenuRepo sysMenuRepo;

    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    public List<MenuVO> getPlatformMenuList(MenuQueryDTO queryDTO) {
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();

        if (Objects.nonNull(queryDTO.getVisible())) {
            query.eq(SysMenu::getVisible, queryDTO.getVisible());
        }

        query.orderByAsc(SysMenu::getSort);


        return sysMenuRepo.list(query).stream().map(m -> {
            MenuVO menuVO = BeanCopyUtils.copyBean(m, MenuVO.class);
            menuVO.setRank(m.getSort());
            return menuVO;
        }).collect(Collectors.toList());
    }

    public List<AsyncRoutesVO> buildMenuTreeByRoles(List<Long> roleIdList) {
        List<SysMenu> sysMenuList = sysMenuMapper.listRoleMenuByRoles(roleIdList, false);
        return buildMenuTree(sysMenuList);
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
        SysMenu sysMenu = sysMenuMapper.selectById(menuId);
        if (Objects.isNull(sysMenu)) {
            throw new BizException("菜单不存在");
        }
        sysMenuMapper.deleteById(sysMenu);
        List<Long> deleteMenuIdList = new ArrayList<>();
        deleteMenuIdList.add(sysMenu.getId());
        List<SysMenu> childList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, sysMenu.getId()));
        if (!childList.isEmpty()) {
            List<Long> childIdList = childList.stream().map(SysMenu::getId).collect(Collectors.toList());

            sysMenuMapper.deleteBatchIds(childIdList);
            deleteMenuIdList.addAll(childIdList);
        }
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getMenuId, deleteMenuIdList));
        return true;
    }

    /**
     * 根据菜单列表构建菜单树
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    public List<AsyncRoutesVO> buildMenuTree(List<SysMenu> menuList) {
        List<AsyncRoutesVO> rootNodes = new ArrayList<>();
        for (SysMenu menu : menuList) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootNodes.add(buildMenuNode(menu, menuList));
            }
        }
        // 对根节点进行排序
        rootNodes.sort(Comparator.comparingInt(o -> o.getMeta().getRank()));
        return rootNodes;
    }

    private AsyncRoutesVO buildMenuNode(SysMenu menu, List<SysMenu> menuList) {
        // 前端所需字段
        AsyncRoutesVO node = new AsyncRoutesVO();
        node.setPath(menu.getPath());
        node.setName(menu.getName());
        node.setComponent(menu.getComponent());
        node.setRedirect(menu.getRedirect());
        node.setType(menu.getMenuType());
        // 设置路由元信息
        AsyncRoutesMetaVO meta = new AsyncRoutesMetaVO();
        meta.setTitle(menu.getTitle());
        meta.setIcon(menu.getIcon());
        meta.setRank(menu.getSort());
        meta.setShowLink(BooleanEnum.fromValue(menu.getShowLink()));
        meta.setShowParent(BooleanEnum.fromValue(menu.getShowParent()));
        meta.setKeepAlive(BooleanEnum.fromValue(menu.getKeepAlive()));
        meta.setFrameLoading(BooleanEnum.fromValue(menu.getFrameLoading()));
        meta.setAuths(Optional.ofNullable(menu.getAuths())
                .map(List::of)
                .orElse(Collections.emptyList()));
        meta.setFrameSrc(menu.getFrameSrc());
        node.setMeta(meta);
        // 递归构建子节点
        List<AsyncRoutesVO> children = new ArrayList<>();
        for (SysMenu childMenu : menuList) {
            if (childMenu.getParentId() != null && childMenu.getParentId().equals(menu.getId())) {
                children.add(buildMenuNode(childMenu, menuList));
            }
        }
        children.sort(Comparator.comparingInt(o -> o.getMeta().getRank()));
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
     * @return com.homi.model.entity.SysMenu
     */
    public SysMenu getMenuById(Long id) {
        return sysMenuMapper.selectById(id);
    }

    public Boolean createMenu(MenuCreateDTO dto) {
        SysMenu sysMenu = BeanCopyUtils.copyBean(dto, SysMenu.class);

        sysMenu.setFrameLoading(Boolean.TRUE.equals(dto.getFrameLoading()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
        sysMenu.setKeepAlive(Boolean.TRUE.equals(dto.getKeepAlive()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
        sysMenu.setHiddenTag(Boolean.TRUE.equals(dto.getHiddenTag()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
        sysMenu.setFixedTag(Boolean.TRUE.equals(dto.getFixedTag()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
        sysMenu.setShowLink(Boolean.TRUE.equals(dto.getShowLink()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
        sysMenu.setShowParent(Boolean.TRUE.equals(dto.getShowParent()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
        sysMenu.setIsPlatform(dto.getIsPlatform() ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());

        if (Objects.isNull(dto.getId())) {
            sysMenuRepo.getBaseMapper().insert(sysMenu);
        } else {
            sysMenuRepo.getBaseMapper().updateById(sysMenu);
        }

        return true;
    }

    public void updateById(SysMenu sysMenu) {
        sysMenuRepo.updateById(sysMenu);
    }

    /**
     * 菜单数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/25 14:17
     *
     * @return java.util.List<com.homi.model.entity.SysMenu>
     */
    public List<SysMenu> getPlatformMenuList() {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getIsPlatform, BooleanEnum.TRUE.getValue());

        return sysMenuRepo.getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 根据菜单id列表获取菜单数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/25 14:17
     *
     * @param menuIdList 参数说明
     * @return java.util.List<com.homi.model.entity.SysMenu>
     */
    public List<SysMenu> getMenuByIds(List<Long> menuIdList) {
        return sysMenuMapper.selectBatchIds(menuIdList);
    }
}
