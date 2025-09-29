package com.homi.domain.dto.room;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/9/26
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 楼栋分组
 */
@Data
@Schema(description = "楼栋单元分组")
public class UnitGroup {
    @Schema(description = "楼栋号")
    private String building;

    @Schema(description = "单元号")
    private String unit;

    @Schema(description = "房间数量")
    private Integer roomCount;

    @Schema(description = "已出租房间数量")
    private Integer leasedCount;

    @Schema(description = "出租率")
    private BigDecimal occupancyRate;
}