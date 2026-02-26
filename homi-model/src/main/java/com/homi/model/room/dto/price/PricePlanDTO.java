package com.homi.model.room.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/28
 */

@Data
@Schema(description = "房间租金方案表")
public class PricePlanDTO {

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "租金方案名称")
    private String planName;

    @Schema(description = "租金方案类型（如：长期/短租/节假日）")
    private String planType;

    @Schema(description = "出房价格比例（百分比，如 12.50 表示 12.5%）")
    private BigDecimal priceRatio;

    @Schema(description = "出房价格（若为固定价格）")
    private BigDecimal price;

    @Schema(description = "其他费用")
    private List<OtherFeeDTO> otherFees;

    @Schema(description = "是否默认方案")
    private Boolean defaultPlan;
}
