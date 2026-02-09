package com.homi.model.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量已读DTO")
public class SysReadBatchDTO {
    @Schema(description = "ID列表")
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
