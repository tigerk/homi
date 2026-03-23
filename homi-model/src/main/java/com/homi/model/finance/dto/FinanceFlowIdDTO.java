package com.homi.model.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "财务流水详情查询DTO")
public class FinanceFlowIdDTO {
    @Schema(description = "财务流水ID")
    @NotNull(message = "财务流水ID不能为空")
    private Long id;
}
