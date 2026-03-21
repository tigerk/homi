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

    @Schema(description = "状态：0=待支付、1=待审批、2=支付成功、3=支付失败、4=已关闭、5=退款中、6=已退款")
    private Integer status;

    @Schema(description = "审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回")
    private Integer approvalStatus;

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
