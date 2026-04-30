package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "新增业主签约合同文档DTO")
public class OwnerContractDocCreateDTO {
    @Schema(description = "业主合同主单ID")
    private Long ownerContractId;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;

    @Schema(description = "合同介质")
    private String contractMedium;

    @Schema(description = "备注")
    private String remark;
}
