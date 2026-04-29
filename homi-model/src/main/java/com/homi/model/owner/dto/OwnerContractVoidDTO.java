package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同作废DTO")
public class OwnerContractVoidDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;

    @Schema(description = "作废原因")
    private String voidReason;
}
