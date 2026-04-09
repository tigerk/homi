package com.homi.model.owner.dto;

import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "包租其他费用DTO")
public class OwnerLeaseFeeDTO {
    @Schema(description = "费用科目类型")
    private String feeType;

    @Schema(description = "费用名称")
    private String feeName;

    @Schema(description = "收支方向")
    private FinanceFlowDirectionEnum feeDirection;

    @Schema(description = "付款方式代码")
    private Integer paymentMethod;

    @Schema(description = "价格方式代码")
    private Integer priceMethod;

    @Schema(description = "金额或比例")
    private BigDecimal priceInput;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
