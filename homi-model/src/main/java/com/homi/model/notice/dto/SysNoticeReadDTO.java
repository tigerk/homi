package com.homi.model.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "系统公告已读DTO")
public class SysNoticeReadDTO {
    @Schema(description = "公告ID")
    @NotNull(message = "公告ID不能为空")
    private Long id;
}
