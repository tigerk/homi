package com.homi.service.external.pay;

import com.homi.common.lib.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PayProviderFactory {

    private final Map<String, PayProvider> providerMap;

    public PayProviderFactory(List<PayProvider> providers) {
        this.providerMap = providers.stream().collect(Collectors.toMap(provider -> provider.channel().toLowerCase(), Function.identity()));
    }

    public PayProvider getProvider(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new BizException("支付渠道不能为空");
        }
        PayProvider provider = providerMap.get(channel.toLowerCase());
        if (provider == null) {
            throw new BizException("不支持的支付渠道: " + channel);
        }
        return provider;
    }
}
