package com.homi.service.external.pay.wechat;

import cn.hutool.core.lang.Pair;
import com.homi.service.external.pay.dto.PayQrCodeDTO;
import com.homi.service.external.pay.PayProvider;
import com.homi.service.external.pay.config.PayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WechatPayService implements PayProvider {

    private final PayProperties payProperties;

    @Override
    public String channel() {
        return "wechat";
    }

    /**
     * 生成支付二维码
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/6 13:43
     *
     * @param payQrCodeDTO 参数说明
     * @return java.lang.String
     */
    @Override
    public Pair<String, String> genQrcode(PayQrCodeDTO payQrCodeDTO) {
        return Pair.of(payQrCodeDTO.getOrderNo(), payQrCodeDTO.getNotifyUrl());
    }

}
