package com.homi.model.finance.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "财务流水查询DTO")
public class FinanceFlowFinanceQueryDTO extends PageDTO {
    @Schema(description = "财务流水业务类型")
    private String bizType;

    @Schema(description = "财务流水状态：0=入账中，1=已入账，3=已作废")
    private Integer status;

    @Schema(description = "流水类型")
    private String flowType;

    @Schema(description = "费用类型")
    private String feeType;

    @Schema(description = "房源/对象/账单关键词")
    private String roomKeyword;
}
