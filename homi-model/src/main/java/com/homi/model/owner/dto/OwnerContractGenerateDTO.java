package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同重新生成DTO")
public class OwnerContractGenerateDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;
}
