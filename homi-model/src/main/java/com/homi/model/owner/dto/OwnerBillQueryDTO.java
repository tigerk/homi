package com.homi.model.owner.dto;

import com.homi.common.lib.dto.PageDTO;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业主账单查询DTO")
public class OwnerBillQueryDTO extends PageDTO {
    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "账单编号")
    private String billNo;

    @Schema(description = "合作模式")
    private OwnerCooperationModeEnum cooperationMode;

    @Schema(description = "审批状态")
    private Integer approvalStatus;

    @Schema(description = "结算状态")
    private Integer settlementStatus;
}
