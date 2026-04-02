package com.homi.model.owner.dto;

import com.homi.common.lib.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同状态更新DTO")
public class OwnerContractStatusDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;

    @Schema(description = "状态")
    private StatusEnum status;
}
