package com.homi.domain.vo.room.grid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/9/29
 */

@Data
@Schema(description = "房间聚合数据")
public class RoomAggregatedVO {
    @Schema(description = "模式来源id，集中式为集中式id，整租、合租为community_id")
    private Long modeRefId;

    @Schema(description = "楼栋")
    private String building;

    @Schema(description = "单元")
    private String unit;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "总房间数")
    private Integer roomCount;

    @Schema(description = "已租房间数")
    private Integer leasedCount;

    @Schema(description = "出租率")
    private BigDecimal occupancyRate;
}
