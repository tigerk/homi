package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 租客签约合同文档。
 * <p>
 * lease 表表示一次租客租约；本表表示该租约下可签署的一份合同文档。
 * </p>
 *
 * @author tk
 * @since 2025-12-15
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease_contract_doc")
@Schema(name = "LeaseContract", description = "租客签约合同文档")
public class LeaseContract implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租客合同ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "租约ID")
    @TableField("lease_id")
    private Long leaseId;

    @Schema(description = "签约合同文档编号")
    @TableField("doc_no")
    private String docNo;

    @Schema(description = "合同模板ID")
    @TableField("contract_template_id")
    private Long contractTemplateId;

    @Schema(description = "合同内容")
    @TableField("contract_content")
    private String contractContent;

    @Schema(description = "签约状态：0=待签字、1=已签字")
    @TableField("sign_status")
    private Integer signStatus;

    @Schema(description = "合同介质")
    @TableField("contract_medium")
    private String contractMedium;

    @Schema(description = "文档状态：1=有效，-1=已作废")
    @TableField("doc_status")
    private Integer docStatus;

    @Schema(description = "作废原因")
    @TableField("void_reason")
    private String voidReason;

    @Schema(description = "作废操作人ID")
    @TableField("void_by")
    private Long voidBy;

    @Schema(description = "作废时间")
    @TableField("void_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voidAt;

    @Schema(description = "合同签约备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
