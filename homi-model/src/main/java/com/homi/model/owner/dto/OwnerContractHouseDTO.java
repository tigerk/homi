package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同房源DTO")
public class OwnerContractHouseDTO {
    @Schema(description = "合同房源ID")
    private Long id;

    @Schema(description = "房源ID")
    private Long houseId;

    @Schema(description = "房源名称")
    private String houseName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "轻托管结算规则")
    private OwnerSettlementRuleDTO settlementRule;

    @Schema(description = "轻托管免租规则")
    private OwnerRentFreeRuleDTO rentFreeRule;
}
