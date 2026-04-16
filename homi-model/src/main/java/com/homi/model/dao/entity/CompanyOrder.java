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
 * 企业购买订单表
 * </p>
 *
 * @author tk
 * @since 2026-03-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_order")
@Schema(name = "CompanyOrder", description = "企业购买订单表")
public class CompanyOrder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "购买订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "商品ID")
    @TableField("product_id")
    private Long productId;

    @Schema(description = "商品编码（冗余）")
    @TableField("product_code")
    private String productCode;

    @Schema(description = "商品名称（冗余）")
    @TableField("product_name")
    private String productName;

    @Schema(description = "下单时单价（元）")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @Schema(description = "购买数量")
    @TableField("quantity")
    private Integer quantity;

    @Schema(description = "订单总金额（元）")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "配额有效期（NULL表示永不过期）")
    @TableField("expire_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireDate;

    @Schema(description = "订单状态：1待支付，2已支付，3已取消，4已退款")
    @TableField("status")
    private Integer status;

    @Schema(description = "取消时间")
    @TableField("cancel_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelAt;

    @Schema(description = "退款金额（元）")
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @Schema(description = "退款时间")
    @TableField("refund_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date refundAt;

    @Schema(description = "支付方式：1线上支付，2线下转账，3后台代付")
    @TableField("pay_method")
    private Integer payMethod;

    @Schema(description = "支付渠道：alipay/wechat/bank")
    @TableField("pay_channel")
    private String payChannel;

    @Schema(description = "第三方交易流水号")
    @TableField("transaction_no")
    private String transactionNo;

    @Schema(description = "支付完成时间")
    @TableField("pay_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payAt;

    @Schema(description = "支付回调通知时间")
    @TableField("notify_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date notifyAt;

    @Schema(description = "操作人ID（后台代购时填写）")
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

    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
