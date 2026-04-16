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
 * 业主个人信息表
 * </p>
 *
 * @author tk
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_personal")
@Schema(name = "OwnerPersonal", description = "业主个人信息表")
public class OwnerPersonal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "姓名")
    @TableField("name")
    private String name;

    @Schema(description = "性别：0=未知，1=男，2=女")
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

    @Schema(description = "收款人姓名")
    @TableField("payee_name")
    private String payeeName;

    @Schema(description = "收款人电话")
    @TableField("payee_phone")
    private String payeePhone;

    @Schema(description = "收款人证件类型")
    @TableField("payee_id_type")
    private Integer payeeIdType;

    @Schema(description = "收款人证件号码")
    @TableField("payee_id_no")
    private String payeeIdNo;

    @Schema(description = "银行卡开户名")
    @TableField("bank_account_name")
    private String bankAccountName;

    @Schema(description = "银行卡号")
    @TableField("bank_account_no")
    private String bankAccountNo;

    @Schema(description = "开户行名称")
    @TableField("bank_name")
    private String bankName;

    @Schema(description = "标签JSON数组")
    @TableField("tags")
    private String tags;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "状态：1=启用，0=禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否删除：0=否，1=是")
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
