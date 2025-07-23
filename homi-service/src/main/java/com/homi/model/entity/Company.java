package com.homi.model.entity;

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
 * 公司表
 * </p>
 *
 * @author tk
 * @since 2025-07-23
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company")
@Schema(name = "Company", description = "公司表")
public class Company implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "公司名称")
    @TableField("name")
    private String name;

    @Schema(description = "城市编码")
    @TableField("city_code")
    private String cityCode;

    @Schema(description = "公司LOGO")
    @TableField("logo")
    private String logo;

    @Schema(description = "公司简称")
    @TableField("abbr")
    private String abbr;

    @Schema(description = "公司网站")
    @TableField("website")
    private String website;

    @Schema(description = "联系人")
    @TableField("contact_name")
    private String contactName;

    @Schema(description = "联系人手机号")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "邮箱号")
    @TableField("email")
    private String email;

    @Schema(description = "账号数量")
    @TableField("account_count")
    private Integer accountCount;

    @Schema(description = "法人姓名")
    @TableField("legal_person")
    private String legalPerson;

    @Schema(description = "公司社会统一信用代码")
    @TableField("uscc")
    private String uscc;

    @Schema(description = "通信地址")
    @TableField("address")
    private String address;

    @Schema(description = "公司性质 1：企业 2：个人")
    @TableField("nature")
    private Integer nature;

    @Schema(description = "公司套餐id")
    @TableField("package_id")
    private Long packageId;

    @Schema(description = "状态（1正常，0禁用）")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
