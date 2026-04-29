package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String cooperationMode;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;

    @Schema(description = "合同内容快照")
    private String contractContent;

    @Schema(description = "签署状态")
    private Integer signStatus;

    @Schema(description = "签约类型")
    private String signType;

    @Schema(description = "合同介质")
    private String contractMedium;

    @Schema(description = "是否通知业主")
    private Boolean notifyOwner;

    @Schema(description = "合同开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractStart;

    @Schema(description = "合同结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractEnd;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "审批状态")
    private Integer approvalStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "原业主合同ID")
    private Long parentContractId;

    @Schema(description = "合同性质：1=新签，2=续约")
    private Integer contractNature;

    @Schema(description = "续约来源合同编号快照")
    private String renewFromContractNo;

    @Schema(description = "退房状态：0=未退房，1=已退房")
    private Integer checkoutStatus;

    @Schema(description = "退房日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkoutDate;

    @Schema(description = "退房原因")
    private String checkoutReason;

    @Schema(description = "退房操作人ID")
    private Long checkoutBy;

    @Schema(description = "退房操作人名称")
    private String checkoutByName;

    @Schema(description = "退房操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkoutAt;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
