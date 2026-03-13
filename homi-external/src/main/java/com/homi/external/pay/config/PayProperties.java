package com.homi.external.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pay")
public class PayProperties {

    /**
     * 默认渠道：yeepay/alipay/wechat
     */
    private String defaultChannel = "yeepay";

    private Provider provider = new Provider();

    @Data
    public static class Provider {
        private ChannelConfig yeepay = new ChannelConfig();
        private ChannelConfig alipay = new ChannelConfig();
        private ChannelConfig wechat = new ChannelConfig();
    }

    @Data
    public static class ChannelConfig {
        private boolean enabled;
        // 上级商户号
        private String parentMerchantNo;
        private String appId;
        private String privateKey;
        private String publicKey;
        private String notifyUrl;
    }
}
