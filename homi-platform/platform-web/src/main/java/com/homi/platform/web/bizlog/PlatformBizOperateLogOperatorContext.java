package com.homi.platform.web.bizlog;

import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
import com.homi.service.bizlog.BizOperateLogOperatorContext;
import org.springframework.stereotype.Component;

@Component
public class PlatformBizOperateLogOperatorContext implements BizOperateLogOperatorContext {
    @Override
    public Long getOperatorId() {
        try {
            return PlatformLoginManager.getCurrentUser().getId();
        } catch (Exception _) {
            return null;
        }
    }

    @Override
    public String getOperatorName() {
        try {
            PlatformUserLoginVO currentUser = PlatformLoginManager.getCurrentUser();
            return currentUser.getUsername();
        } catch (Exception _) {
            return null;
        }
    }

    @Override
    public Long getCompanyId() {
        try {
            return PlatformLoginManager.getCurrentUser().getCompanyId();
        } catch (Exception _) {
            return null;
        }
    }
}
