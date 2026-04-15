package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.finance.PaymentFlowChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "包租业主应付单付款记录VO")
public class OwnerPayableBillPaymentVO {
    @Schema(description = "付款记录ID")
    private Long paymentId;

    @Schema(description = "付款单号")
    private String paymentNo;

    @Schema(description = "付款金额")
    private BigDecimal payAmount;

    @Schema(description = "付款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payAt;

    @Schema(description = "付款渠道")
    private PaymentFlowChannelEnum payChannel;

    @Schema(description = "第三方流水号")
    private String thirdTradeNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "凭证地址")
    private List<String> voucherUrls;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;
}
