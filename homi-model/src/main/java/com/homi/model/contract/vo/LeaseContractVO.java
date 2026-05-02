package com.homi.model.contract.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.model.common.dto.FileAttachGroupDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/29
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "租客签约合同文档 VO")
@Builder
public class LeaseContractVO implements Serializable {
    @Schema(description = "租客合同ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "租约ID")
    @TableField("lease_id")
    private Long leaseId;

    @Schema(description = "签约合同文档编号")
    private String docNo;

    @Schema(description = "合同编号，兼容旧前端字段，等同 docNo")
    private String contractCode;

    @Schema(description = "合同模板ID")
    @TableField("contract_template_id")
    private Long contractTemplateId;
    private String contractTemplateName;

    @Schema(description = "合同内容")
    @TableField("contract_content")
    private String contractContent;

    @Schema(description = "签约状态：0=待签字、1=已签字")
    @TableField("sign_status")
    private Integer signStatus;

    @Schema(description = "合同介质")
    private String contractMedium;

    @Schema(description = "文档状态：1=有效，-1=已作废")
    private Integer docStatus;

    @Schema(description = "租约状态")
    private Integer status;

    @Schema(description = "合同开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractStart;

    @Schema(description = "合同结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractEnd;

    @Schema(description = "合同附件列表")
    private List<String> contractAttachmentList;

    @Schema(description = "合同附件分组列表")
    private List<FileAttachGroupDTO> contractAttachmentGroupList;

    @Schema(description = "作废原因")
    private String voidReason;

    @Schema(description = "作废操作人ID")
    private Long voidBy;

    @Schema(description = "作废操作人名称")
    private String voidByName;

    @Schema(description = "作废时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voidAt;

    @Schema(description = "合同签约备注")
    @TableField("remark")
    private String remark;

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
