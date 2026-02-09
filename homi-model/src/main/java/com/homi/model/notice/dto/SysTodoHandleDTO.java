package com.homi.model.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "待办处理DTO")
public class SysTodoHandleDTO {
    @Schema(description = "待办ID")
    @NotNull(message = "待办ID不能为空")
    private Long id;

    @Schema(description = "处理备注")
    @NotBlank(message = "处理备注不能为空")
    private String handleRemark;
}
