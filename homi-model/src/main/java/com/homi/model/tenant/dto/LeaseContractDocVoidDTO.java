package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "作废租客签约合同文档DTO")
public class LeaseContractDocVoidDTO {
    @Schema(description = "租客签约合同文档ID")
    private Long leaseContractDocId;

    @Schema(description = "作废原因")
    private String voidReason;
}
