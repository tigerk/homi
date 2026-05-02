package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主签约合同作废DTO")
public class OwnerContractDocVoidDTO {
    @Schema(description = "业主合同签约文档ID")
    private Long ownerContractDocId;

    @Schema(description = "作废原因")
    private String voidReason;
}
