package com.homi.model.owner.dto;

import com.homi.common.lib.enums.finance.FinanceFlowDirectionEnum;
import com.homi.common.lib.enums.price.PaymentMethodEnum;
import com.homi.common.lib.enums.price.PriceMethodEnum;
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

    @Schema(description = "付款方式")
    private PaymentMethodEnum paymentMethod;

    @Schema(description = "价格方式")
    private PriceMethodEnum priceMethod;

    @Schema(description = "金额或比例")
    private BigDecimal priceInput;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
