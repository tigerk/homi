package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退租费用明细表
 *
 * @author tk
 * @since 2026-02-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease_checkout_fee")
@Schema(name = "LeaseCheckoutFee", description = "退租费用明细表")
public class LeaseCheckoutFee implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "退租单ID")
    @TableField("checkout_id")
    private Long checkoutId;

    @Schema(description = "收支类型：1=收（租客应付），2=支（退还租客）")
    @TableField("fee_direction")
    private Integer feeDirection;

    @Schema(description = "费用类型")
    @TableField("fee_type")
    private Integer feeType;

    @Schema(description = "费用子类名称（如'房屋押金'）")
    @TableField("fee_sub_name")
    private String feeSubName;

    @Schema(description = "费用金额（正数）")
    @TableField("fee_amount")
    private BigDecimal feeAmount;

    @Schema(description = "费用周期开始")
    @TableField("fee_period_start")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feePeriodStart;

    @Schema(description = "费用周期结束")
    @TableField("fee_period_end")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feePeriodEnd;

    @Schema(description = "费用备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "关联账单ID（如有）")
    @TableField("bill_id")
    private Long billId;

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
