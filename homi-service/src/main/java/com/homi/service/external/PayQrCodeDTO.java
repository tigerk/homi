package com.homi.service.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/3/6
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayQrCodeDTO implements Serializable {
    @Schema(description = "商户号")
    private String merchantNo;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "通知地址")
    private String notifyUrl;
}
