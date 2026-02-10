package com.homi.model.monitor;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/7/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "操作日志DTO")
public class OperationLogDTO extends PageDTO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "模块")
    private String title;

    @Schema(description = "操作状态")
    private Integer status;

    @Schema(description = "操作时间")
    private List<OffsetDateTime> requestTime;

    @Schema(description = "操作账号")
    private String username;
}
