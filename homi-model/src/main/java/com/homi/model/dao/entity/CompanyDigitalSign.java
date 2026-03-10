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
 * 企业电子签章
 * </p>
 *
 * @author tk
 * @since 2026-03-10
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_digital_sign")
@Schema(name = "CompanyDigitalSign", description = "企业电子签章")
public class CompanyDigitalSign implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "签章类型：1=企业，2=个人")
    @TableField("sign_type")
    private Integer signType;

    @Schema(description = "公司名称")
    @TableField("name")
    private String name;

    @Schema(description = "公司社会统一信用代码")
    @TableField("uscc")
    private String uscc;

    @Schema(description = "法人姓名")
    @TableField("legal_person")
    private String legalPerson;

    @Schema(description = "法人证件类型")
    @TableField("legal_person_id_type")
    private String legalPersonIdType;

    @Schema(description = "法人证件号")
    @TableField("legal_person_id_no")
    private String legalPersonIdNo;

    @Schema(description = "操作人ID")
    @TableField("operator_id")
    private Long operatorId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
