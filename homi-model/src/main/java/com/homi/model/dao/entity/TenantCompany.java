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
 * 企业租客信息表
 * </p>
 *
 * @author tk
 * @since 2025-11-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_company")
@Schema(name = "TenantCompany", description = "企业租客信息表")
public class TenantCompany implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业租客ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "企业名称")
    @TableField("company_name")
    private String companyName;

    @Schema(description = "统一社会信用代码")
    @TableField("uscc")
    private String uscc;

    @Schema(description = "法定代表人")
    @TableField("legal_person")
    private String legalPerson;

    @Schema(description = "法人证件类型")
    @TableField("legal_person_id_type")
    private Integer legalPersonIdType;

    @Schema(description = "法人证件号码")
    @TableField("legal_person_id_no")
    private String legalPersonIdNo;

    @Schema(description = "联系人姓名")
    @TableField("contact_name")
    private String contactName;

    @Schema(description = "联系电话")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "注册地址")
    @TableField("registered_address")
    private String registeredAddress;

    @Schema(description = "营业执照附件")
    @TableField("business_license_url")
    private String businessLicenseUrl;

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
