package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "新增租客签约合同文档DTO")
public class LeaseContractDocCreateDTO {
    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;

    @Schema(description = "合同介质")
    private String contractMedium;

    @Schema(description = "备注")
    private String remark;
}
