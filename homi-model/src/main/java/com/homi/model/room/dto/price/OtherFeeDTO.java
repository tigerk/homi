package com.homi.model.room.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

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
@Schema(description = "其他费用DTO")
public class OtherFeeDTO implements Serializable {
    @Schema(description = "其他费用类型（如：装修/维修/房屋维修、随房租付、按固定金额等）")
    private Long dictDataId;

    @Schema(description = "其他费用名称")
    private String name;

    @Schema(description = "付款方式（如：随房租付、按固定金额等）")
    private Integer paymentMethod;

    @Schema(description = "价格计算方式")
    private Integer priceMethod;

    @Schema(description = "价格输入值")
    private BigDecimal priceInput;
}
