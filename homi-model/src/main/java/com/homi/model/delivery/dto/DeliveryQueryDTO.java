package com.homi.model.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Data
@Schema(description = "交割单查询DTO")
public class DeliveryQueryDTO {
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "交割单ID")
    private Long id;

    private String subjectType;

    private Long subjectTypeId;

    private Long roomId;

    private String handoverType;

    private Integer status;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;
}
