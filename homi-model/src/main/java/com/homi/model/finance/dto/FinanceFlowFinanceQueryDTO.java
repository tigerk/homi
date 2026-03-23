package com.homi.model.finance.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "租客财务流水查询DTO")
public class FinanceFlowFinanceQueryDTO extends PageDTO {
    @Schema(description = "财务流水状态：0=入账中，1=已入账，3=已作废")
    private Integer status;

    @Schema(description = "流水类型")
    private String flowType;

    @Schema(description = "费用类型")
    private String feeType;

    @Schema(description = "房源信息关键词")
    private String roomKeyword;
}
