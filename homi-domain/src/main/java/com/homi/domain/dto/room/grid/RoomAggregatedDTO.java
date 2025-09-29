package com.homi.domain.dto.room.grid;

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
public class RoomAggregatedDTO {
    @Schema(description = "小区 id")
    private Long communityId;

    @Schema(description = "楼栋")
    private String building;

    @Schema(description = "单元")
    private String unit;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "总房间数")
    private Integer totalRooms;

    @Schema(description = "已租房间数")
    private Integer leasedRooms;

    @Schema(description = "出租率")
    private BigDecimal occupancyRate;
}
