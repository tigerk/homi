package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同签约状态更新DTO")
public class OwnerContractSignStatusUpdateDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;

    @Schema(description = "签署状态 code")
    private Integer signStatus;
}
