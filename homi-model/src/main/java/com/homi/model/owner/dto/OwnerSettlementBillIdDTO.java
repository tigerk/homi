package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "轻托管业主结算单ID DTO")
public class OwnerSettlementBillIdDTO {
    @Schema(description = "结算单ID")
    private Long billId;
}
