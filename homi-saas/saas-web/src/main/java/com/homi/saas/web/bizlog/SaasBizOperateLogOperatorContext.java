package com.homi.saas.web.bizlog;

import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.bizlog.BizOperateLogOperatorContext;
import org.springframework.stereotype.Component;

@Component
public class SaasBizOperateLogOperatorContext implements BizOperateLogOperatorContext {
    @Override
    public Long getOperatorId() {
        try {
            return LoginManager.getCurrentUser().getId();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String getOperatorName() {
        try {
            UserLoginVO currentUser = LoginManager.getCurrentUser();
            return currentUser.getUsername();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public Long getCompanyId() {
        try {
            return LoginManager.getCurrentUser().getCurCompanyId();
        } catch (Exception ignored) {
            return null;
        }
    }
}
