package com.homi.model.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 操作人DTO
 */
@Data
@Builder
public class OperatorDTO {
    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;
}
