package com.homi.model.owner.dto;

import com.homi.common.lib.enums.owner.OwnerWithdrawOperateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主提现操作DTO")
public class OwnerWithdrawOperateDTO {
    @Schema(description = "提现申请ID")
    private Long applyId;

    @Schema(description = "操作类型")
    private OwnerWithdrawOperateEnum operateType;

    @Schema(description = "失败或驳回原因")
    private String failureReason;

    @Schema(description = "第三方交易号")
    private String thirdTradeNo;

    @Schema(description = "打款渠道")
    private String channel;
}
