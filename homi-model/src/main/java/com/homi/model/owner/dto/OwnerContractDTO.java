package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.approval.BizApprovalStatusEnum;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import com.homi.common.lib.enums.owner.OwnerSignStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "业主合同DTO")
public class OwnerContractDTO {
    @Schema(description = "合同ID")
    private Long id;

    @Schema(description = "SaaS企业ID")
    private Long companyId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "合作模式")
    private OwnerCooperationModeEnum cooperationMode;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;

    @Schema(description = "合同内容快照")
    private String contractContent;

    @Schema(description = "签署状态")
    private OwnerSignStatusEnum signStatus;

    @Schema(description = "合同开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractStart;

    @Schema(description = "合同结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractEnd;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "审批状态")
    private BizApprovalStatusEnum approvalStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
