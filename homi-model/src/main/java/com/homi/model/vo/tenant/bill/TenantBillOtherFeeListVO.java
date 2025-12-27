package com.homi.model.vo.tenant.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/26
 */
@Data
@ToString(callSuper = true)
@Schema(description = "租客账单VO")
public class TenantBillOtherFeeListVO implements Serializable {
    @Schema(description = "其他费用明细ID")
    private Long id;

    @Schema(description = "租客账单 ID")
    private Long billId;

    @Schema(description = "其他费用类型（如：装修/维修/房屋维修、随房租付、按固定金额等）")
    private Long dictDataId;

    @Schema(description = "其他费用名称")
    private String name;

    @Schema(description = "付款方式（如：随房租付、按固定金额等）")
    private Integer paymentMethod;

    @Schema(description = "价格计算方式")
    private Integer priceMethod;

    @Schema(description = "价格输入值")
    private Integer priceInput;
}
