package com.homi.domain.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

public class HouseLayoutDTO {
    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "房型名称")
    private String layoutName;

    @Schema(description = "厅")
    private Integer livingRoom;

    @Schema(description = "卫")
    private Integer bathroom;

    @Schema(description = "厨")
    private Integer kitchen;

    @Schema(description = "室")
    private Integer bedroom;

    @Schema(description = "面积")
    private BigDecimal insideSpace;

    @Schema(description = "朝向")
    private Integer orientation;
}
