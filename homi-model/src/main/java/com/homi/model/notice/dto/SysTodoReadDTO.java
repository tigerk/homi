package com.homi.model.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "待办已读DTO")
public class SysTodoReadDTO {
    @Schema(description = "待办ID")
    @NotNull(message = "待办ID不能为空")
    private Long id;
}
