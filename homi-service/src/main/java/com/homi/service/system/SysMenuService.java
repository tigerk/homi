package com.homi.service.system;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.menu.MenuQueryDTO;
import com.homi.domain.enums.common.BooleanEnum;
import com.homi.domain.vo.menu.AsyncRoutesMetaVO;
import com.homi.domain.vo.menu.AsyncRoutesVO;
import com.homi.domain.vo.menu.SimpleMenuVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysMenu;
import com.homi.model.entity.SysRoleMenu;
import com.homi.model.mapper.SysMenuMapper;
import com.homi.model.mapper.SysRoleMenuMapper;
import com.homi.model.repo.SysMenuRepo;
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
    public List<SysMenu> getMenuList(MenuQueryDTO queryDTO) {
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();

        if (Objects.nonNull(queryDTO.getVisible())) {
            query.eq(SysMenu::getVisible, queryDTO.getVisible());
        }

        query.orderByAsc(SysMenu::getSortOrder);

        return sysMenuRepo.list(query);
    }


    public List<AsyncRoutesVO> buildMenuTreeByRoles(List<Long> roleIdList) {
        List<SysMenu> sysMenuList = sysMenuMapper.listRoleMenuByRoles(roleIdList, false);
        return buildMenuTree(sysMenuList);
    }

    public List<SimpleMenuVO> listSimpleMenu() {
        MenuQueryDTO menuQueryDTO = new MenuQueryDTO();
        menuQueryDTO.setVisible(BooleanEnum.FALSE.getValue());
        List<SysMenu> menuList = getMenuList(menuQueryDTO);

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
    private List<AsyncRoutesVO> buildMenuTree(List<SysMenu> menuList) {
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
        meta.setRank(menu.getSortOrder());
        meta.setShowLink(Boolean.TRUE);
        meta.setKeepAlive(BooleanEnum.fromValue(menu.getCacheFlag()));
        meta.setFrameLoading(BooleanEnum.fromValue(menu.getFrameLoading()));
        meta.setAuths(Optional.ofNullable(menu.getPerms())
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

    public Boolean save(SysMenu sysMenu) {
        return sysMenuRepo.save(sysMenu);
    }

    public void updateById(SysMenu sysMenu) {
        sysMenuRepo.updateById(sysMenu);
    }
}
