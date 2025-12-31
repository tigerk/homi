package com.homi.platform.web.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlatformSaTokenPermConfig implements StpInterface {
    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession sessionByLoginId = StpUtil.getSessionByLoginId(loginId);
        PlatformUserLoginVO loginVO = (PlatformUserLoginVO) sessionByLoginId.get(SaSession.USER);

        return loginVO.getPermissions();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession sessionByLoginId = StpUtil.getSessionByLoginId(loginId);
        PlatformUserLoginVO loginVO = (PlatformUserLoginVO) sessionByLoginId.get(SaSession.USER);

        return loginVO.getRoles();
    }
}
