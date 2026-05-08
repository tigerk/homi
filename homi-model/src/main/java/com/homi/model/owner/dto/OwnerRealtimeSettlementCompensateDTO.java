package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "轻托管实时分账补偿DTO")
public class OwnerRealtimeSettlementCompensateDTO {
    @Schema(description = "业主合同ID；为空时补偿当前公司全部已签字轻托管合同")
    private Long contractId;
}
