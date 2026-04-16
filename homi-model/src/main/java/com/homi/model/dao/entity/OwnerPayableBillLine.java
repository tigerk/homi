package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_payable_bill_line")
@Schema(name = "OwnerPayableBillLine", description = "包租业主应付单明细表")
public class OwnerPayableBillLine implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "应付单ID")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "来源类型")
    @TableField("source_type")
    private String sourceType;

    @Schema(description = "来源ID")
    @TableField("source_id")
    private Long sourceId;

    @Schema(description = "合同房源名称快照")
    @TableField("subject_name_snapshot")
    private String subjectNameSnapshot;

    @Schema(description = "明细类型")
    @TableField("item_type")
    private String itemType;

    @Schema(description = "明细名称")
    @TableField("item_name")
    private String itemName;

    @Schema(description = "方向")
    @TableField("direction")
    private String direction;

    @Schema(description = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "业务时间")
    @TableField("biz_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date bizDate;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "公式快照")
    @TableField("formula_snapshot")
    private String formulaSnapshot;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;
}
