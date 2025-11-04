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
 * 统一交易流水表
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("trade_payment_record")
@Schema(name = "TradePaymentRecord", description = "统一交易流水表")
public class TradePaymentRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @TableField("order_id")
    private Long orderId;

    @TableField("payer_type")
    private Integer payerType;

    @TableField("payer_id")
    private Long payerId;

    @Schema(description = "支付流水号")
    @TableField("payment_no")
    private String paymentNo;

    @Schema(description = "第三方交易号")
    @TableField("external_trade_no")
    private String externalTradeNo;

    @Schema(description = "1=支付，2=退款")
    @TableField("payment_type")
    private Integer paymentType;

    @TableField("payment_method")
    private Integer paymentMethod;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("currency")
    private String currency;

    @TableField("payment_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date paymentTime;

    @TableField("confirm_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date confirmTime;

    @Schema(description = "0=待确认，1=成功，2=失败，3=已退款")
    @TableField("payment_status")
    private Integer paymentStatus;

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
