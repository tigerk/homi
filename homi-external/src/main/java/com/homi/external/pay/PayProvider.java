package com.homi.external.pay;

import cn.hutool.core.lang.Pair;
import com.homi.external.pay.dto.PayQrCodeDTO;

public interface PayProvider {

    /**
     * 支付渠道编码
     */
    String channel();

    /**
     * 生成支付二维码
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/3/6 13:43
     *
     * @param payQrCodeDTO 参数说明
     * @return java.lang.String
     */
    Pair<String, String> genQrcode(PayQrCodeDTO payQrCodeDTO);
}
