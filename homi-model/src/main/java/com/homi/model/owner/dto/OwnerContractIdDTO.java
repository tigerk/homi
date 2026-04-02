package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同ID DTO")
public class OwnerContractIdDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;
}
