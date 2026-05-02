package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租客签约合同文档ID DTO")
public class LeaseContractDocIdDTO {
    @Schema(description = "租客签约合同文档ID")
    private Long leaseContractDocId;

    @Schema(description = "租约ID，兼容旧预览/下载入口")
    private Long leaseId;
}
