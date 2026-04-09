package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.finance.PaymentFlowChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "业主账单付款登记DTO")
public class OwnerBillPaymentCreateDTO {
    @Schema(description = "业主账单ID")
    private Long billId;

    @Schema(description = "付款金额")
    private BigDecimal payAmount;

    @Schema(description = "付款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "付款渠道")
    private PaymentFlowChannelEnum payChannel;

    @Schema(description = "第三方流水号")
    private String thirdTradeNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "支付凭证URL列表")
    private List<String> voucherUrls;
}
