package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "租客账单其他费用DTO")
public class LeaseBillOtherFeeDTO {
    @Schema(description = "费用字典 ID")
    private Long dictDataId;

    @Schema(description = "费用项目名称（如 租金、水费、电费）")
    private String name;

    @Schema(description = "费用金额")
    private BigDecimal amount;

    @Schema(description = "备注")
    private String remark;
}
