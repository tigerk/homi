package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租客账单作废DTO")
public class LeaseBillVoidDTO {
    @Schema(description = "账单ID")
    private Long billId;

    @Schema(description = "作废原因")
    private String voidReason;

    @Schema(description = "操作人ID")
    private Long updateBy;
}
