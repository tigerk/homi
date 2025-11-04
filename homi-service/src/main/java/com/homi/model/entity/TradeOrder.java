package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 统一交易订单表（租客/房东/平台/第三方支付）
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("trade_order")
@Schema(name = "TradeOrder", description = "统一交易订单表（租客/房东/平台/第三方支付）")
public class TradeOrder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "交易订单ID")
    @TableId("id")
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @Schema(description = "支付主体类型：1=租客，2=房东，3=平台，4=第三方")
    @TableField("payer_type")
    private Integer payerType;

    @Schema(description = "支付主体ID")
    @TableField("payer_id")
    private Long payerId;

    @Schema(description = "订单编号")
    @TableField("order_no")
    private String orderNo;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态：0=待支付，1=支付中，2=已支付，3=已取消")
    @TableField("order_status")
    private Integer orderStatus;

    @Schema(description = "支付方式")
    @TableField("payment_method")
    private Integer paymentMethod;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
