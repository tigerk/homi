package com.homi.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.menu.MenuQueryDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.vo.menu.SimpleMenuVO;
import com.homi.model.entity.SysMenu;
import com.homi.service.system.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */


@RequiredArgsConstructor
@Slf4j
@RequestMapping("admin/sys/menu")
public class SysMenuController {
    private final SysMenuService sysMenuService;

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @SaCheckPermission("system:menu:query")
    public ResponseResult<List<SysMenu>> listMenu(MenuQueryDTO queryDTO) {

        return ResponseResult.ok(sysMenuService.getMenuList(queryDTO));
    }

    /**
     * 返回简单菜单列表，树由前端构建（用户菜单）
     *
     * @return 所有数据
     */
    @GetMapping("/list/simple")
    public ResponseResult<List<SimpleMenuVO>> listSimple() {
        List<SimpleMenuVO> simpleMenuVOS = sysMenuService.listSimpleMenu();
        return ResponseResult.ok(simpleMenuVOS);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/get/{id}")
    @SaCheckPermission("system:menu:detail")
    public ResponseResult<SysMenu> selectOne(@PathVariable Long id) {
        return ResponseResult.ok(sysMenuService.getMenuById(id));
    }

    /**
     * 新增菜单
     *
     * @param sysMenu 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @SaCheckPermission("system:menu:create")
    public ResponseResult<Boolean> insert(@RequestBody SysMenu sysMenu) {
        sysMenu.setCreateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        return ResponseResult.ok(sysMenuService.save(sysMenu));
    }

    /**
     * 修改数据
     *
     * @param sysMenu 实体对象
     * @return 修改结果
     */
    @PutMapping("/update")
    @SaCheckPermission("system:menu:update")
    public ResponseResult<Void> update(@RequestBody SysMenu sysMenu) {
        if (Objects.nonNull(sysMenu.getParentId()) && sysMenu.getParentId().equals(sysMenu.getId())) {
            return ResponseResult.fail(ResponseCodeEnum.VALID_ERROR.getCode(), "父菜单不能为本身");
        }
        sysMenu.setUpdateBy(Long.valueOf(StpUtil.getLoginId().toString()));
        this.sysMenuService.updateById(sysMenu);
        return ResponseResult.ok();
    }

    /**
     * 删除菜单
     *
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:menu:delete")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.ok(sysMenuService.deleteById(id));
    }
}
