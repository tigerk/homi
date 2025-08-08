package com.homi.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.lang.Pair;
import com.homi.admin.auth.service.AuthService;
import com.homi.domain.dto.menu.AsyncRoutesVO;
import com.homi.model.entity.User;
import com.homi.service.system.SysRoleService;
import com.homi.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SaTokenPermsConfig implements StpInterface {
    private final AuthService authService;

    private final UserService userService;

    private final SysRoleService sysRoleService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        User userById = userService.getUserById(Long.valueOf(loginId.toString()));

        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = authService.getUserAuth(userById);

        return userAuth.getRight();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User userById = userService.getUserById(Long.valueOf(loginId.toString()));

        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = authService.getUserAuth(userById);

        return userAuth.getLeft().getValue();
    }
}
