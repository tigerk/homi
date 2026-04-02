package com.homi.external.sign.fadada;

import com.fadada.sdk.base.client.FddBaseClient;
import com.fadada.sdk.base.model.req.RegisterAccountParams;
import com.homi.common.lib.enums.contract.ContractSealSourceEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.external.sign.dto.SignAccountRegisterDTO;
import com.homi.external.sign.SignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/4/2
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FadadaClient implements SignProvider {

    private final FadadaProperties fadadaProperties;

    @Override
    public Integer getSource() {
        return ContractSealSourceEnum.FADADA.getCode();
    }

    @Override
    public String registerAccount(SignAccountRegisterDTO request) {
        validateConfig();
        validateRequest(request);

        FddBaseClient baseClient = new FddBaseClient(
            fadadaProperties.getAppId(),
            fadadaProperties.getAppSecret(),
            fadadaProperties.getVersion(),
            normalizeHost(fadadaProperties.getHost())
        );
        RegisterAccountParams params = new RegisterAccountParams();
        params.setAccountType(request.getAccountType());
        params.setOpenId(request.getOpenId());

        String result = baseClient.invokeRegisterAccount(params);
        log.info("Fadada registerAccount success, openId={}, result={}", request.getOpenId(), result);
        return result;
    }

    private void validateConfig() {
        if (!fadadaProperties.isEnabled()) {
            throw new BizException("法大大电子签未启用");
        }
        if (!StringUtils.hasText(fadadaProperties.getAppId())) {
            throw new BizException("法大大 appId 未配置");
        }
        if (!StringUtils.hasText(fadadaProperties.getAppSecret())) {
            throw new BizException("法大大 appSecret 未配置");
        }
        if (!StringUtils.hasText(fadadaProperties.getHost())) {
            throw new BizException("法大大 host 未配置");
        }
    }

    private void validateRequest(SignAccountRegisterDTO request) {
        if (request == null) {
            throw new BizException("签章注册请求不能为空");
        }
        if (!StringUtils.hasText(request.getAccountType())) {
            throw new BizException("账号类型不能为空");
        }
        if (!StringUtils.hasText(request.getOpenId())) {
            throw new BizException("openId 不能为空");
        }
    }

    private String normalizeHost(String host) {
        if (!StringUtils.hasText(host)) {
            return host;
        }
        return host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
    }
}
