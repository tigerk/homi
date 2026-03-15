package com.homi.model.tenant.vo.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
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

    @Schema(description = "支付状态")
    private String status;

    @Schema(description = "第三方单号")
    private String thirdTradeNo;

    @Schema(description = "支付金额（分）")
    private Long amount;

    @Schema(description = "支付完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;
}
