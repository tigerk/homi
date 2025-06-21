package com.homi.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.menu.MenuCreateDTO;
import com.homi.domain.dto.menu.MenuQueryDTO;
import com.homi.domain.enums.common.BooleanEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.vo.menu.SimpleMenuVO;
import com.homi.model.entity.SysMenu;
import com.homi.model.repo.SysMenuRepo;
import com.homi.service.system.SysMenuService;
import com.homi.utils.BeanCopyUtils;
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

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
//    @SaCheckPermission("system:menu:query")
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
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/21 16:04
     *
     * @param createDTO 参数说明
     * @return com.homi.domain.base.ResponseResult<java.lang.Boolean>
     */
    @PostMapping("/create")
//    @SaCheckPermission("system:menu:create")
    public ResponseResult<Boolean> createMenu(@RequestBody MenuCreateDTO createDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        if (Objects.isNull(createDTO.getId())) {
            createDTO.setCreateBy(currentUser.getId());
            createDTO.setCreateTime(DateUtil.date());
        }

        return ResponseResult.ok(sysMenuService.createMenu(createDTO));
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


    @PostMapping("initialize")
    public Boolean initialize() {
        String json = """
                  [
                    {
                      "parentId": 0,
                      "id": 100,
                      "menuType": 0,
                      "title": "menus.pureExternalPage",
                      "name": "PureIframe",
                      "path": "/iframe",
                      "component": "",
                      "rank": 7,
                      "redirect": "",
                      "icon": "ri:links-fill",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 100,
                      "id": 101,
                      "menuType": 0,
                      "title": "menus.pureExternalDoc",
                      "name": "PureIframeExternal",
                      "path": "/iframe/external",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 101,
                      "id": 102,
                      "menuType": 2,
                      "title": "menus.pureExternalLink",
                      "name": "https://pure-admin.cn/",
                      "path": "/external",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 101,
                      "id": 103,
                      "menuType": 2,
                      "title": "menus.pureUtilsLink",
                      "name": "https://pure-admin-utils.netlify.app/",
                      "path": "/pureUtilsLink",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 100,
                      "id": 104,
                      "menuType": 1,
                      "title": "menus.pureEmbeddedDoc",
                      "name": "PureIframeEmbedded",
                      "path": "/iframe/embedded",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 104,
                      "id": 105,
                      "menuType": 1,
                      "title": "menus.pureEpDoc",
                      "name": "FrameEp",
                      "path": "/iframe/ep",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "https://element-plus.org/zh-CN/",
                      "frameLoading": true,
                      "keepAlive": true,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 104,
                      "id": 106,
                      "menuType": 1,
                      "title": "menus.pureTailwindcssDoc",
                      "name": "FrameTailwindcss",
                      "path": "/iframe/tailwindcss",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "https://tailwindcss.com/docs/installation",
                      "frameLoading": true,
                      "keepAlive": true,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 104,
                      "id": 107,
                      "menuType": 1,
                      "title": "menus.pureVueDoc",
                      "name": "FrameVue",
                      "path": "/iframe/vue3",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "https://cn.vuejs.org/",
                      "frameLoading": true,
                      "keepAlive": true,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 104,
                      "id": 108,
                      "menuType": 1,
                      "title": "menus.pureViteDoc",
                      "name": "FrameVite",
                      "path": "/iframe/vite",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "https://cn.vitejs.dev/",
                      "frameLoading": true,
                      "keepAlive": true,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 104,
                      "id": 109,
                      "menuType": 1,
                      "title": "menus.purePiniaDoc",
                      "name": "FramePinia",
                      "path": "/iframe/pinia",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "https://pinia.vuejs.org/zh/index.html",
                      "frameLoading": true,
                      "keepAlive": true,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 104,
                      "id": 110,
                      "menuType": 1,
                      "title": "menus.pureRouterDoc",
                      "name": "FrameRouter",
                      "path": "/iframe/vue-router",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "https://router.vuejs.org/zh/",
                      "frameLoading": true,
                      "keepAlive": true,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 0,
                      "id": 200,
                      "menuType": 0,
                      "title": "menus.purePermission",
                      "name": "PurePermission",
                      "path": "/permission",
                      "component": "",
                      "rank": 9,
                      "redirect": "",
                      "icon": "ep:lollipop",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 200,
                      "id": 201,
                      "menuType": 0,
                      "title": "menus.purePermissionPage",
                      "name": "PermissionPage",
                      "path": "/permission/page/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 200,
                      "id": 202,
                      "menuType": 0,
                      "title": "menus.purePermissionButton",
                      "name": "PermissionButton",
                      "path": "/permission/button",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 202,
                      "id": 203,
                      "menuType": 0,
                      "title": "menus.purePermissionButtonRouter",
                      "name": "PermissionButtonRouter",
                      "path": "/permission/button/router",
                      "component": "permission/button/index",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 203,
                      "id": 210,
                      "menuType": 3,
                      "title": "添加",
                      "name": "",
                      "path": "",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "permission:btn:add",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 203,
                      "id": 211,
                      "menuType": 3,
                      "title": "修改",
                      "name": "",
                      "path": "",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "permission:btn:edit",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 203,
                      "id": 212,
                      "menuType": 3,
                      "title": "删除",
                      "name": "",
                      "path": "",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "permission:btn:delete",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 202,
                      "id": 204,
                      "menuType": 0,
                      "title": "menus.purePermissionButtonLogin",
                      "name": "PermissionButtonLogin",
                      "path": "/permission/button/login",
                      "component": "permission/button/perms",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 204,
                      "id": 220,
                      "menuType": 3,
                      "title": "添加",
                      "name": "",
                      "path": "",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "permission:btn:add",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 204,
                      "id": 221,
                      "menuType": 3,
                      "title": "修改",
                      "name": "",
                      "path": "",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "permission:btn:edit",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 204,
                      "id": 222,
                      "menuType": 3,
                      "title": "删除",
                      "name": "",
                      "path": "",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "permission:btn:delete",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 0,
                      "id": 600,
                      "menuType": 0,
                      "title": "menus.pureTenantManagement",
                      "name": "PureTenant",
                      "path": "/company",
                      "component": "",
                      "rank": 13,
                      "redirect": "",
                      "icon": "ri:home-gear-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 600,
                      "id": 701,
                      "menuType": 0,
                      "title": "menus.pureTenantList",
                      "name": "TenantList",
                      "path": "/company/list/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:list-check",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 600,
                      "id": 702,
                      "menuType": 0,
                      "title": "menus.pureTenantPackage",
                      "name": "TenantPackage",
                      "path": "/company/package/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:file-paper-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 0,
                      "id": 300,
                      "menuType": 0,
                      "title": "menus.pureSysManagement",
                      "name": "PureSystem",
                      "path": "/system",
                      "component": "",
                      "rank": 10,
                      "redirect": "",
                      "icon": "ri:settings-3-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 300,
                      "id": 301,
                      "menuType": 0,
                      "title": "menus.pureUser",
                      "name": "SystemUser",
                      "path": "/system/user/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:admin-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 300,
                      "id": 302,
                      "menuType": 0,
                      "title": "menus.pureRole",
                      "name": "SystemRole",
                      "path": "/system/role/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:admin-fill",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 300,
                      "id": 303,
                      "menuType": 0,
                      "title": "menus.pureSystemMenu",
                      "name": "SystemMenu",
                      "path": "/system/menu/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "ep:menu",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 300,
                      "id": 304,
                      "menuType": 0,
                      "title": "menus.pureDept",
                      "name": "SystemDept",
                      "path": "/system/dept/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:git-branch-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 300,
                      "id": 305,
                      "menuType": 0,
                      "title": "menus.pureDict",
                      "name": "SystemDict",
                      "path": "/system/dict/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:book-2-line",
                      "extraIcon": "IF-pure-iconfont-new svg",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 0,
                      "id": 400,
                      "menuType": 0,
                      "title": "menus.pureSysMonitor",
                      "name": "PureMonitor",
                      "path": "/monitor",
                      "component": "",
                      "rank": 11,
                      "redirect": "",
                      "icon": "ep:monitor",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 400,
                      "id": 401,
                      "menuType": 0,
                      "title": "menus.pureOnlineUser",
                      "name": "OnlineUser",
                      "path": "/monitor/online-user",
                      "component": "monitor/online/index",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:user-voice-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 400,
                      "id": 402,
                      "menuType": 0,
                      "title": "menus.pureLoginLog",
                      "name": "LoginLog",
                      "path": "/monitor/login-logs",
                      "component": "monitor/logs/login/index",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:window-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 400,
                      "id": 403,
                      "menuType": 0,
                      "title": "menus.pureOperationLog",
                      "name": "OperationLog",
                      "path": "/monitor/operation-logs",
                      "component": "monitor/logs/operation/index",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:history-fill",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 400,
                      "id": 404,
                      "menuType": 0,
                      "title": "menus.pureSystemLog",
                      "name": "SystemLog",
                      "path": "/monitor/system-logs",
                      "component": "monitor/logs/system/index",
                      "rank": null,
                      "redirect": "",
                      "icon": "ri:file-search-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 0,
                      "id": 500,
                      "menuType": 0,
                      "title": "menus.pureTabs",
                      "name": "PureTabs",
                      "path": "/tabs",
                      "component": "",
                      "rank": 12,
                      "redirect": "",
                      "icon": "ri:bookmark-2-line",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 500,
                      "id": 501,
                      "menuType": 0,
                      "title": "menus.pureTabs",
                      "name": "Tabs",
                      "path": "/tabs/index",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": true,
                      "showParent": false
                    },
                    {
                      "parentId": 500,
                      "id": 502,
                      "menuType": 0,
                      "title": "query传参模式",
                      "name": "TabQueryDetail",
                      "path": "/tabs/query-detail",
                      "component": "",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "/tabs/index",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": false,
                      "showParent": false
                    },
                    {
                      "parentId": 500,
                      "id": 503,
                      "menuType": 0,
                      "title": "params传参模式",
                      "name": "TabParamsDetail",
                      "path": "/tabs/params-detail/:id",
                      "component": "params-detail",
                      "rank": null,
                      "redirect": "",
                      "icon": "",
                      "extraIcon": "",
                      "enterTransition": "",
                      "leaveTransition": "",
                      "activePath": "/tabs/index",
                      "auths": "",
                      "frameSrc": "",
                      "frameLoading": true,
                      "keepAlive": false,
                      "hiddenTag": false,
                      "fixedTag": false,
                      "showLink": false,
                      "showParent": false
                    }
                  ]
                """;

        List<MenuCreateDTO> list = JSONUtil.toList(json, MenuCreateDTO.class);
        for (MenuCreateDTO dto : list) {
            dto.setUpdateBy(1L);
            dto.setUpdateTime(DateUtil.date());
            dto.setCreateBy(1L);
            dto.setCreateTime(DateUtil.date());

            SysMenu sysMenu = BeanCopyUtils.copyBean(dto, SysMenu.class);

            sysMenu.setFrameLoading(Boolean.TRUE.equals(dto.getFrameLoading()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
            sysMenu.setKeepAlive(Boolean.TRUE.equals(dto.getKeepAlive()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
            sysMenu.setHiddenTag(Boolean.TRUE.equals(dto.getHiddenTag()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
            sysMenu.setFixedTag(Boolean.TRUE.equals(dto.getFixedTag()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
            sysMenu.setShowLink(Boolean.TRUE.equals(dto.getShowLink()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());
            sysMenu.setShowParent(Boolean.TRUE.equals(dto.getShowParent()) ? BooleanEnum.TRUE.getValue() : BooleanEnum.FALSE.getValue());

            sysMenuRepo.getBaseMapper().insert(sysMenu);
        }

        return Boolean.TRUE;
    }
}
