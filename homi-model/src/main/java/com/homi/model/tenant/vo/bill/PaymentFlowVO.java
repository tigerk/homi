package com.homi.model.tenant.vo.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "支付流水简要信息")
public class PaymentFlowVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "系统支付流水号")
    private String paymentNo;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务单据ID")
    private Long bizId;

    @Schema(description = "支付渠道")
    private String channel;

    @Schema(description = "状态：0=支付中、1=支付成功、2=支付失败、3=已关闭、4=退款中、5=已退款")
    private Integer status;

    @Schema(description = "第三方单号")
    private String thirdTradeNo;

    @Schema(description = "支付凭证图片")
    private String paymentVoucherUrl;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付备注")
    private String remark;

    @Schema(description = "支付完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;
}
