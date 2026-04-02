package com.homi.model.owner.dto;

import com.homi.common.lib.dto.PageDTO;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.owner.OwnerSignStatusEnum;
import com.homi.common.lib.enums.owner.OwnerTypeEnum;
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
    private OwnerTypeEnum ownerType;

    @Schema(description = "签署状态")
    private OwnerSignStatusEnum signStatus;

    @Schema(description = "状态")
    private StatusEnum status;
}
