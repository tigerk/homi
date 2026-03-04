package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 企业充值记录表
 * </p>
 *
 * @author tk
 * @since 2026-03-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_recharge")
@Schema(name = "CompanyRecharge", description = "企业充值记录表")
public class CompanyRecharge implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "充值订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "充值金额（元）")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "赠送金额（元）")
    @TableField("bonus_amount")
    private BigDecimal bonusAmount;

    @Schema(description = "实际到账金额（元）")
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @Schema(description = "充值前余额（元）")
    @TableField("before_balance")
    private BigDecimal beforeBalance;

    @Schema(description = "充值后余额（元）")
    @TableField("after_balance")
    private BigDecimal afterBalance;

    @Schema(description = "支付方式：1线上支付，2线下转账，3后台充值")
    @TableField("pay_method")
    private Integer payMethod;

    @Schema(description = "支付渠道：alipay/wechat/bank")
    @TableField("pay_channel")
    private String payChannel;

    @Schema(description = "第三方交易流水号")
    @TableField("transaction_no")
    private String transactionNo;

    @Schema(description = "状态：1待支付，2支付中，3成功，4失败")
    @TableField("status")
    private Integer status;

    @Schema(description = "支付完成时间")
    @TableField("pay_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "操作人ID（后台充值时填写）")
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
