package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.model.common.dto.FileAttachGroupDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "业主合同签约文档DTO")
public class OwnerContractDocDTO {
    @Schema(description = "签约合同文档ID")
    private Long id;

    @Schema(description = "SaaS企业ID")
    private Long companyId;

    @Schema(description = "业主合同主单ID")
    private Long ownerContractId;

    @Schema(description = "签约合同文档编号")
    private String docNo;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;

    @Schema(description = "合同模板名称")
    private String contractTemplateName;

    @Schema(description = "合同内容快照")
    private String contractContent;

    @Schema(description = "合同附件列表")
    private List<String> contractAttachmentList;

    @Schema(description = "合同附件分组列表")
    private List<FileAttachGroupDTO> contractAttachmentGroupList;

    @Schema(description = "签署状态")
    private Integer signStatus;

    @Schema(description = "合同介质")
    private String contractMedium;

    @Schema(description = "文档状态")
    private Integer docStatus;

    @Schema(description = "业主合同主单状态")
    private Integer status;

    @Schema(description = "合同开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractStart;

    @Schema(description = "合同结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractEnd;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "作废原因")
    private String voidReason;

    @Schema(description = "作废操作人ID")
    private Long voidBy;

    @Schema(description = "作废操作人名称")
    private String voidByName;

    @Schema(description = "作废时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voidAt;

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
