package com.homi.model.owner.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业主提现申请查询DTO")
public class OwnerWithdrawApplyQueryDTO extends PageDTO {
    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "提现单号")
    private String applyNo;

    @Schema(description = "审批状态")
    private Integer approvalStatus;

    @Schema(description = "打款状态")
    private Integer withdrawStatus;
}
