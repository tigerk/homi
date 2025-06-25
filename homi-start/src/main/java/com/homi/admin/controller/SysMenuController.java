package com.homi.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.menu.MenuCreateDTO;
import com.homi.domain.dto.menu.MenuQueryDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.vo.menu.MenuVO;
import com.homi.domain.vo.menu.SimpleMenuVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysMenu;
import com.homi.model.entity.SysRoleMenu;
import com.homi.model.repo.SysMenuRepo;
import com.homi.model.repo.SysRoleMenuRepo;
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
@RestController
@RequestMapping("admin/sys/menu")
public class SysMenuController {
    private final SysMenuService sysMenuService;

    private final SysMenuRepo sysMenuRepo;
    private final SysRoleMenuRepo sysRoleMenuRepo;

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @PostMapping("/list")
//    @SaCheckPermission("system:menu:query")
    public ResponseResult<List<MenuVO>> listMenu(MenuQueryDTO queryDTO) {
        return ResponseResult.ok(sysMenuService.getPlatformMenuList(queryDTO));
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
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/21 16:04
     *
     * @param createDTO 参数说明
     * @return com.homi.domain.base.ResponseResult<java.lang.Boolean>
     */
    @PostMapping("/create")
//    @SaCheckPermission("system:menu:create")
    public ResponseResult<Void> createMenu(@RequestBody MenuCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        if (Objects.isNull(createDTO.getId())) {
            createDTO.setCreateBy(currentUser.getId());
            createDTO.setCreateTime(DateUtil.date());
        } else {
            if (Objects.nonNull(createDTO.getParentId()) && createDTO.getParentId().equals(createDTO.getId())) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "父菜单不能为本身");
            }
        }

        sysMenuService.createMenu(createDTO);
        return ResponseResult.ok();
    }

    /**
     * 删除菜单
     *
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
//    @SaCheckPermission("system:menu:delete")
    public ResponseResult<Boolean> delete(@PathVariable("id") Long id) {
        return ResponseResult.ok(sysMenuService.deleteById(id));
    }

    @GetMapping("init")
    public Boolean init() {
        List<SysMenu> list = sysMenuRepo.list();
        for (SysMenu sysMenu : list) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(1L);
            sysRoleMenu.setMenuId(sysMenu.getId());

            sysRoleMenuRepo.save(sysRoleMenu);
        }

        return true;
    }
}
