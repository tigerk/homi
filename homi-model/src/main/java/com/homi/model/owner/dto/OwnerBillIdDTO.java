package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主账单ID DTO")
public class OwnerBillIdDTO {
    @Schema(description = "业主账单ID")
    private Long billId;
}
