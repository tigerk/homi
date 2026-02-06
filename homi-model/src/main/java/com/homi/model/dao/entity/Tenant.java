package com.homi.model.dao.entity;

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
 * 租客表
 * </p>
 *
 * @author tk
 * @since 2025-12-15
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant")
@Schema(name = "Tenant", description = "租客表")
public class Tenant implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租客 ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "租客类型关联ID")
    @TableField("tenant_type_id")
    private Long tenantTypeId;

    @Schema(description = "租客类型：0=个人，1=企业")
    @TableField("tenant_type")
    private Integer tenantType;

    @Schema(description = "租客名称（冗余字段，便于查询）")
    @TableField("tenant_name")
    private String tenantName;

    @Schema(description = "租客联系电话（冗余字段）")
    @TableField("tenant_phone")
    private String tenantPhone;

    @Schema(description = "租客状态：0=停用，1=正常")
    @TableField("status")
    private Integer status;

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
