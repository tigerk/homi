package com.homi.model.owner.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "包租业主应付单查询DTO")
public class OwnerPayableBillQueryDTO extends PageDTO {
    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "应付单号")
    private String billNo;

    @Schema(description = "付款状态")
    private Integer paymentStatus;

    @Schema(description = "单据状态")
    private Integer billStatus;
}
