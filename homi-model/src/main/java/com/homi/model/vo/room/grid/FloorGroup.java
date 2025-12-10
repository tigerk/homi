package com.homi.model.vo.room.grid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/9/26
 */

@Data
@Schema(description = "楼层分组")
public class FloorGroup {
    @Schema(description = "楼层号")
    private Integer floor;

    @Schema(description = "房间数量")
    private Integer roomCount;

    @Schema(description = "出租数量")
    private Integer leasedCount;

    @Schema(description = "出租率")
    private BigDecimal occupancyRate;
}
