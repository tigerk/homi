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
 * 租客个人信息表
 * </p>
 *
 * @author tk
 * @since 2025-12-15
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_personal")
@Schema(name = "TenantPersonal", description = "租客个人信息表")
public class TenantPersonal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租客ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "租客姓名")
    @TableField("name")
    private String name;

    @Schema(description = "性别：1=男，2=女")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "证件类型：0=身份证，1=护照，2=港澳通行证，3=台胞证")
    @TableField("id_type")
    private Integer idType;

    @Schema(description = "证件号码")
    @TableField("id_no")
    private String idNo;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "租客标签")
    @TableField("tags")
    private String tags;

    @Schema(description = "租客备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "租客状态：0=停用，1=启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否删除：0=否，1=是")
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
