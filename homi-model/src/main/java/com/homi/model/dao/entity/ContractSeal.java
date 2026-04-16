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
 * 合同电子印章
 * </p>
 *
 * @author tk
 * @since 2026-03-10
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("contract_seal")
@Schema(name = "ContractSeal", description = "合同电子印章")
public class ContractSeal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "印章类型:1=企业,2=个人")
    @TableField("seal_type")
    private Integer sealType;

    @Schema(description = "来源:1=自有图片,2=法大大,3=E签宝,4=其他第三方")
    @TableField("source")
    private Integer source;

    @Schema(description = "公司名称")
    @TableField("company_name")
    private String companyName;

    @Schema(description = "公司社会统一信用代码")
    @TableField("company_uscc")
    private String companyUscc;

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

    @Schema(description = "状态:0=待审核,1=正常,2=已禁用,3=审核失败")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
