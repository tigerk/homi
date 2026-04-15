package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
@TableName("owner_settlement_item")
@Schema(name = "OwnerSettlementItem", description = "轻托管分账费用科目规则表")
public class OwnerSettlementItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @TableField("contract_id")
    private Long contractId;

    @TableField("contract_subject_id")
    private Long contractSubjectId;

    @TableField("fee_direction")
    private String feeDirection;

    @TableField("fee_type")
    private String feeType;

    @TableField("item_name")
    private String itemName;

    @TableField("transfer_enabled")
    private Boolean transferEnabled;

    @TableField("transfer_ratio")
    private BigDecimal transferRatio;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("remark")
    private String remark;

    @TableField("status")
    private Integer status;

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
