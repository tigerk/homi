package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;

/**
 * <p>
 * 交易订单与账单关联表
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("trade_order_bill")
@Schema(name = "TradeOrderBill", description = "交易订单与账单关联表")
public class TradeOrderBill implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("bill_id")
    private Long billId;

    @TableField("allocated_amount")
    private BigDecimal allocatedAmount;
}
