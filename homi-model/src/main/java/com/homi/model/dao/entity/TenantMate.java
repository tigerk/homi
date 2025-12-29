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
 * 同住人信息表
 * </p>
 *
 * @author tk
 * @since 2025-12-29
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_mate")
@Schema(name = "TenantMate", description = "同住人信息表")
public class TenantMate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "同住人ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "租客ID")
    @TableId("tenant_id")
    private Long tenantId;

    @Schema(description = "姓名")
    @TableField("name")
    private String name;

    @Schema(description = "性别：0=男，1=女")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "证件类型：1=身份证，2=护照")
    @TableField("id_type")
    private Integer idType;

    @Schema(description = "证件号码")
    @TableField("id_no")
    private String idNo;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "标签列表")
    @TableField("tags")
    private String tags;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "状态：0=停用，1=启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "逻辑删除：0=未删除，1=已删除")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
