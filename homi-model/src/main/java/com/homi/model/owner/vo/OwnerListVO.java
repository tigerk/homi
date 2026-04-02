package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import com.homi.common.lib.enums.owner.OwnerSignStatusEnum;
import com.homi.common.lib.enums.owner.OwnerTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "业主列表VO")
public class OwnerListVO {
    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "业主类型")
    private OwnerTypeEnum ownerType;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "业主联系电话")
    private String ownerPhone;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合作模式")
    private OwnerCooperationModeEnum cooperationMode;

    @Schema(description = "合同模板名称")
    private String contractTemplateName;

    @Schema(description = "房源名称列表")
    private String houseNames;

    @Schema(description = "合同开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractStart;

    @Schema(description = "合同结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractEnd;

    @Schema(description = "签署状态")
    private OwnerSignStatusEnum signStatus;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
