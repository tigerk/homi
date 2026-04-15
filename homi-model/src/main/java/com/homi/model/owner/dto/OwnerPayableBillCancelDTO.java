package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "包租业主应付单作废DTO")
public class OwnerPayableBillCancelDTO {
    @Schema(description = "应付单ID")
    private Long billId;

    @Schema(description = "作废原因")
    private String cancelReason;
}
