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

/**
 * <p>
 * 租客账单费用明细表
 * </p>
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease_bill_fee")
@Schema(name = "LeaseBillFee", description = "租客账单费用明细表")
public class LeaseBillFee implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "账单ID（关联 lease_bill.id）")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "费用类型：RENTAL/DEPOSIT/OTHER_FEE")
    @TableField("fee_type")
    private String feeType;

    @Schema(description = "费用字典 ID")
    @TableField("dict_data_id")
    private Long dictDataId;

    @Schema(description = "费用名称")
    @TableField("fee_name")
    private String feeName;

    @Schema(description = "费用金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "已收金额")
    @TableField("paid_amount")
    private BigDecimal paidAmount;

    @Schema(description = "待收金额")
    @TableField("unpaid_amount")
    private BigDecimal unpaidAmount;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付")
    @TableField("pay_status")
    private Integer payStatus;

    @Schema(description = "费用周期开始日期")
    @TableField("fee_start")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeStart;

    @Schema(description = "费用周期结束日期")
    @TableField("fee_end")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeEnd;

    @Schema(description = "备注信息")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除：0=否，1=是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "修改人ID")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "修改时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
