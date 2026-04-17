package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "轻托管分账费用科目DTO")
public class OwnerSettlementItemDTO {
    @Schema(description = "收支方向: IN/OUT")
    private String feeDirection;

    @Schema(description = "费用科目类型")
    private String feeType;

    @Schema(description = "费用科目名称")
    private String feeName;

    @Schema(description = "是否转给业主")
    private Boolean transferEnabled;

    @Schema(description = "转给业主比例")
    private BigDecimal transferRatio;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
