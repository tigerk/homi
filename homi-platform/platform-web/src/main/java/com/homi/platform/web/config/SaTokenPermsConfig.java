package com.homi.platform.web.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.lang.Pair;
import com.homi.model.dao.entity.PlatformUser;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.platform.service.service.perms.PlatformUserService;
import com.homi.platform.web.service.PlatformAuthService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SaTokenPermsConfig implements StpInterface {
    private final PlatformAuthService platformAuthService;

    private final PlatformUserService platformUserService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        PlatformUser platformUserById = platformUserService.getUserById(Long.valueOf(loginId.toString()));

        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = platformAuthService.getUserAuth(platformUserById);

        return userAuth.getRight();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        PlatformUser platformUserById = platformUserService.getUserById(Long.valueOf(loginId.toString()));

        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = platformAuthService.getUserAuth(platformUserById);

        return userAuth.getLeft().getValue();
    }
}
