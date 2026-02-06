package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 租客合同表
 * </p>
 *
 * @author tk
 * @since 2025-12-15
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease_contract")
@Schema(name = "LeaseContract", description = "租客合同表")
public class LeaseContract implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租客合同ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "租约ID")
    @TableField("lease_id")
    private Long leaseId;
    
    @Schema(description = "合同编号")
    @TableField("contract_code")
    private String contractCode;

    @Schema(description = "合同模板ID")
    @TableField("contract_template_id")
    private Long contractTemplateId;

    @Schema(description = "合同内容")
    @TableField("contract_content")
    private String contractContent;

    @Schema(description = "签约状态：0=待签字、1=已签字")
    @TableField("sign_status")
    private Integer signStatus;

    @Schema(description = "合同签约备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
