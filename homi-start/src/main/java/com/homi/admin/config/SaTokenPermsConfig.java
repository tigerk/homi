package com.homi.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.lang.Pair;
import com.homi.admin.auth.service.AuthService;
import com.homi.service.system.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaTokenPermsConfig implements StpInterface {
    private final AuthService authService;

    private final SysRoleService sysRoleService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Pair<List<Long>, ArrayList<String>> roleList = authService.getRoleList(Long.valueOf(loginId.toString()));

        return sysRoleService.getMenuPermissionByRoles(roleList.getKey());
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return authService.getRoleList(Long.valueOf(loginId.toString())).getValue();

    }
}
