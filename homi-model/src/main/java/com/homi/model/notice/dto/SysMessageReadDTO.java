package com.homi.model.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "站内信已读DTO")
public class SysMessageReadDTO {
    @Schema(description = "消息ID")
    @NotNull(message = "消息ID不能为空")
    private Long id;
}
