package com.homi.model.company.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "企业订购记录")
public class CompanyOrderRecordVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品编码")
    private String productCode;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "状态：1待支付，2已支付，3已取消，4已退款")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "支付方式：1线上支付，2线下转账，3后台代付")
    private Integer payMethod;

    @Schema(description = "支付方式名称")
    private String payMethodName;

    @Schema(description = "支付渠道：alipay/wechat/bank")
    private String payChannel;

    @Schema(description = "第三方交易流水号")
    private String transactionNo;

    @Schema(description = "购买时间（已支付取支付时间，否则取创建时间）")
    private Date purchaseTime;

    @Schema(description = "支付完成时间")
    private Date payTime;

    @Schema(description = "支付回调通知时间")
    private Date notifyTime;

    @Schema(description = "备注")
    private String remark;
}
