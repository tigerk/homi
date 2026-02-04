package com.homi.saas.web.auth.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.model.dao.entity.UserWechat;
import com.homi.model.dao.repo.UserWechatRepo;
import com.homi.saas.web.auth.dto.login.LoginDTO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.WechatMiniappProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/**
 * 微信小程序登录
 *
 * @author tk
 * @since 2026-02-04
 */
@Service
@RequiredArgsConstructor
public class WechatAuthService {
    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final WechatMiniappProperties wechatMiniappProperties;
    private final UserWechatRepo userWechatRepo;
    private final AuthService authService;

    private final RestTemplate restTemplate = new RestTemplate();

    public UserLoginVO loginByCode(String code) {
        WechatSession session = getSessionByCode(code);
        UserWechat bind = userWechatRepo.getByOpenIdAndAppId(session.getOpenid(), wechatMiniappProperties.getAppId());
        if (bind == null) {
            throw new BizException(ResponseCodeEnum.WECHAT_NOT_BIND);
        }
        return authService.loginByUserId(bind.getUserId());
    }

    public UserLoginVO bindAndLogin(String code, String username, String password) {
        WechatSession session = getSessionByCode(code);
        String appId = wechatMiniappProperties.getAppId();

        Long userId = getUserIdByUsernamePassword(username, password);

        UserWechat byOpenId = userWechatRepo.getByOpenIdAndAppId(session.getOpenid(), appId);
        if (byOpenId != null && !byOpenId.getUserId().equals(userId)) {
            throw new BizException(ResponseCodeEnum.WECHAT_BIND_CONFLICT);
        }

        UserWechat byUser = userWechatRepo.getByUserIdAndAppId(userId, appId);

        if (byOpenId == null) {
            if (byUser == null) {
                UserWechat userWechat = new UserWechat();
                userWechat.setUserId(userId);
                userWechat.setOpenId(session.getOpenid());
                userWechat.setUnionId(session.getUnionid());
                userWechat.setAppId(appId);
                userWechat.setCreateTime(new Date());
                userWechatRepo.save(userWechat);
            } else {
                byUser.setOpenId(session.getOpenid());
                byUser.setUnionId(session.getUnionid());
                userWechatRepo.updateById(byUser);
            }
        }

        return authService.loginByUserId(userId);
    }

    private Long getUserIdByUsernamePassword(String username, String password) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);
        UserLoginVO userLoginVO = authService.checkUserLogin(loginDTO);
        return userLoginVO.getId();
    }

    private WechatSession getSessionByCode(String code) {
        if (CharSequenceUtil.isBlank(code)) {
            throw new BizException(ResponseCodeEnum.VALID_ERROR);
        }

        String appId = wechatMiniappProperties.getAppId();
        String appSecret = wechatMiniappProperties.getAppSecret();
        if (CharSequenceUtil.isBlank(appId) || CharSequenceUtil.isBlank(appSecret)) {
            throw new BizException(ResponseCodeEnum.WECHAT_LOGIN_ERROR);
        }

        String url = JSCODE2SESSION_URL + "?appid=" + appId + "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
        String body = restTemplate.getForObject(url, String.class);
        WechatSession session = JSONUtil.toBean(body, WechatSession.class);
        if (session == null || session.getErrcode() != null && session.getErrcode() != 0) {
            throw new BizException(ResponseCodeEnum.WECHAT_LOGIN_ERROR);
        }
        if (CharSequenceUtil.isBlank(session.getOpenid())) {
            throw new BizException(ResponseCodeEnum.WECHAT_LOGIN_ERROR);
        }
        return session;
    }

    @Data
    private static class WechatSession {
        private String openid;
        private String unionid;
        private String session_key;
        private Integer errcode;
        private String errmsg;
    }
}
