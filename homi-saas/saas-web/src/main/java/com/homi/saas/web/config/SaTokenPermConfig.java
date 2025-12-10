package com.homi.saas.web.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.saas.web.auth.service.AuthService;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.service.service.system.RoleService;
import com.homi.saas.service.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SaTokenPermConfig implements StpInterface {
    private final AuthService authService;

    private final UserService userService;

    private final RoleService roleService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession sessionByLoginId = StpUtil.getSessionByLoginId(loginId);
        UserLoginVO loginVO = (UserLoginVO) sessionByLoginId.get(SaSession.USER);

        return loginVO.getPermissions();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession sessionByLoginId = StpUtil.getSessionByLoginId(loginId);
        UserLoginVO loginVO = (UserLoginVO) sessionByLoginId.get(SaSession.USER);

        return loginVO.getRoles();
    }
}
