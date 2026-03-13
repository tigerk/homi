package com.homi.external.pay.yeepay;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.exception.BizException;
import com.homi.external.pay.dto.PayQrCodeDTO;
import com.homi.external.pay.PayProvider;
import com.homi.external.pay.config.PayProperties;
import com.yeepay.yop.sdk.service.aggpay.AggpayClient;
import com.yeepay.yop.sdk.service.aggpay.AggpayClientBuilder;
import com.yeepay.yop.sdk.service.aggpay.model.OrderCodeResponseDTO;
import com.yeepay.yop.sdk.service.aggpay.request.PayLinkRequest;
import com.yeepay.yop.sdk.service.aggpay.response.PayLinkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 易宝支付占位实现。
 * TODO: 后续替换为真实易宝 SDK 下单逻辑，返回真实收银台/二维码链接。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YeepayService implements PayProvider {
    private final PayProperties payProperties;

    private final AggpayClient aggpayClient = AggpayClientBuilder.builder().build();

    @Override
    public String channel() {
        return "yeepay";
    }

    @Override
    public Pair<String, String> genQrcode(PayQrCodeDTO payQrCodeDTO) {
        PayProperties.ChannelConfig cfg = payProperties.getProvider().getYeepay();
        if (!cfg.isEnabled()) {
            throw new BizException("易宝支付未启用，请检查配置");
        }

        PayLinkRequest request = new PayLinkRequest();
        request.setParentMerchantNo(cfg.getParentMerchantNo());
        request.setMerchantNo(payQrCodeDTO.getMerchantNo());
        request.setOrderId(payQrCodeDTO.getOrderNo());
        request.setOrderAmount(payQrCodeDTO.getAmount());
        // 设置过期时间
        cn.hutool.core.date.DateTime dateTime = DateUtil.offsetMinute(DateUtil.date(), 60);
        org.joda.time.DateTime expiredTime = new org.joda.time.DateTime(dateTime.getTime());
        request.setExpiredTime(expiredTime);
        request.setGoodsName(payQrCodeDTO.getTitle());
        request.setFundProcessType("DELAY_SETTLE");
        request.setScene("OFFLINE");
        request.setLimitCredit("N");
        request.setNotifyUrl(payQrCodeDTO.getNotifyUrl());
        request.setCsUrl(payQrCodeDTO.getNotifyUrl());
        log.info("易宝支付请求参数-{}", JSONUtil.toJsonStr(request));
        PayLinkResponse response = aggpayClient.payLink(request);
        OrderCodeResponseDTO result = response.getResult();
        log.error("易宝支付返回信息-{}", JSONUtil.toJsonStr(response));
        if (ObjectUtil.isNotEmpty(result) && "00000".equals(result.getCode())) {
            return Pair.of(result.getUniqueOrderNo(), result.getQrCodeUrl());
        } else {
            throw new BizException("支付信息返回错误");
        }
    }
}
