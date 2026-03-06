package com.homi.service.external.pay;

import cn.hutool.core.lang.Pair;
import com.homi.common.lib.exception.BizException;
import com.homi.service.external.PayQrCodeDTO;
import com.homi.service.external.pay.config.PayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayService {
    private final PayProviderFactory payProviderFactory;
    private final PayProperties payProperties;

    /**
     * 获取支付渠道
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/6 13:33
     *
     * @param channel 参数说明
     * @return com.homi.service.external.pay.PayProvider
     */
    private PayProvider getProvider(String channel) {
        if (channel == null || channel.isBlank()) {
            return payProviderFactory.getProvider(payProperties.getDefaultChannel());
        }
        return payProviderFactory.getProvider(channel);
    }

    /**
     * 生成支付二维码
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/6 13:51
     *
     * @param payQrCodeDTO 参数说明
     * @param channel      参数说明
     * @return cn.hutool.core.lang.Pair<java.lang.String,java.lang.String> 第三方交易流水号、二维码支付地址。
     */
    public Pair<String, String> genQrcode(PayQrCodeDTO payQrCodeDTO, String channel) {
        if (payQrCodeDTO == null) {
            throw new BizException("支付二维码参数不能为空");
        }
        PayProvider provider = getProvider(channel);
        return provider.genQrcode(payQrCodeDTO);
    }
}
