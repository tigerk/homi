package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同签约文档ID DTO")
public class OwnerContractDocIdDTO {
    @Schema(description = "业主合同签约文档ID")
    private Long ownerContractDocId;
}
