package com.homi.model.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支付流水作废DTO")
public class PaymentFlowVoidDTO {
    @Schema(description = "支付流水ID")
    private Long id;

    @Schema(description = "作废原因")
    private String voidReason;

    @Schema(description = "操作人ID")
    private Long updateBy;
}
