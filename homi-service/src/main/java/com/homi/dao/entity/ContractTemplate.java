package com.homi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 合同模板表
 * </p>
 *
 * @author tk
 * @since 2025-11-18
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("contract_template")
@Schema(name = "ContractTemplate", description = "合同模板表")
public class ContractTemplate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "合同模板ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "合同模板类型：1=租客、2=业主、3=预定")
    @TableField("contract_type")
    private Integer contractType;

    @Schema(description = "合同模板名称")
    @TableField("template_name")
    private String templateName;

    @Schema(description = "合同模板内容，包含模板变量占位符")
    @TableField("template_content")
    private String templateContent;

    @Schema(description = "生效部门json")
    @TableField("dept_ids")
    private String deptIds;

    @Schema(description = "合同状态：0=未生效，1=生效中，-1=已作废")
    @TableField("status")
    private Integer status;

    @Schema(description = "合同模板备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
