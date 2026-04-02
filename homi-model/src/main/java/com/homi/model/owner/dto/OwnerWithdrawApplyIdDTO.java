package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主提现申请ID DTO")
public class OwnerWithdrawApplyIdDTO {
    @Schema(description = "提现申请ID")
    private Long applyId;
}
