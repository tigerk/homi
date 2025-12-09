package com.homi.dao.entity;

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
 * @since 2025-11-10
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("rental_order_item")
@Schema(name = "RentalOrderItem", description = "交易订单与账单关联表")
public class RentalOrderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("bill_id")
    private Long billId;

    @TableField("allocated_amount")
    private BigDecimal allocatedAmount;
}
