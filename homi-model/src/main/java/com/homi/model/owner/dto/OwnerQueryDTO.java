package com.homi.model.owner.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "业主查询DTO")
public class OwnerQueryDTO extends PageDTO {
    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "业主手机号")
    private String ownerPhone;

    @Schema(description = "业主类型")
    private Integer ownerType;

    @Schema(description = "合作模式")
    private String cooperationMode;

    @Schema(description = "签署状态")
    private Integer signStatus;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "N天内到期合同")
    private Integer expiringDaysWithin;
}
