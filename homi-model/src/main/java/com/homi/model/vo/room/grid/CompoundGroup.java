package com.homi.model.vo.room.grid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/9/26
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "组合模式")
public class CompoundGroup {
    @Schema(description = "模式引用ID")
    private Long leaseModeId;

    @Schema(description = "租赁模式")
    private Integer leaseMode;

    @Schema(description = "卡片显示名称")
    private String displayName;

    @Schema(description = "小区id")
    private Long communityId;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "小区地址")
    private String communityAddress;

    @Schema(description = "总楼栋数")
    private Integer buildingCount;

    @Schema(description = "总楼层数")
    private Integer floorCount;

    @Schema(description = "房间数量")
    private Integer roomCount;

    @Schema(description = "已出租房间数量")
    private Integer leasedCount;

    @Schema(description = "出租率")
    private BigDecimal occupancyRate;
}
