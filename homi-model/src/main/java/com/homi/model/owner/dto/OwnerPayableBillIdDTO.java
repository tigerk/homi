package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "包租业主应付单ID DTO")
public class OwnerPayableBillIdDTO {
    @Schema(description = "应付单ID")
    private Long billId;
}
