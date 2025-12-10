package com.homi.model.dto.room.price;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "价格配置DTO")
public class PriceConfigDTO {
    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "出房价格（单位：元/月）")
    private BigDecimal price;

    @Schema(description = "底价（单位：元/月）")
    private BigDecimal floorPrice;

    @Schema(description = "底价方式：1=固定金额，2=按比例")
    private Integer floorPriceMethod;

    @Schema(description = "底价录入值（金额或比例，具体由 low_price_method 决定）")
    private BigDecimal  floorPriceInput;

    @Schema(description = "其他费用列表")
    private List<OtherFeeDTO> otherFees;

    @Schema(description = "房间租金方案表")
    private List<PricePlanDTO> pricePlans;

}
