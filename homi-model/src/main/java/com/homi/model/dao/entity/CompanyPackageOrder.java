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
 * 套餐订阅订单表
 * </p>
 *
 * @author tk
 * @since 2026-03-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_package_order")
@Schema(name = "CompanyPackageOrder", description = "套餐订阅订单表")
public class CompanyPackageOrder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "订单号")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "套餐ID")
    @TableField("package_id")
    private Long packageId;

    @Schema(description = "套餐名称（冗余）")
    @TableField("package_name")
    private String packageName;

    @Schema(description = "套餐包含房源数（冗余）")
    @TableField("house_count")
    private Integer houseCount;

    @Schema(description = "订单类型：1首购，2续费，3升级")
    @TableField("order_type")
    private Integer orderType;

    @Schema(description = "购买月数")
    @TableField("months")
    private Integer months;

    @Schema(description = "生效日期")
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    @Schema(description = "到期日期")
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    @Schema(description = "升级前套餐ID（升级时填写）")
    @TableField("from_package_id")
    private Long fromPackageId;

    @Schema(description = "升级时原套餐折抵金额")
    @TableField("upgrade_credit")
    private BigDecimal upgradeCredit;

    @Schema(description = "下单时套餐月单价（元）")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @Schema(description = "应付金额（元）")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额（元，扣除折抵后）")
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @Schema(description = "支付方式：1余额，2线上，3线下，4后台")
    @TableField("pay_method")
    private Integer payMethod;

    @Schema(description = "支付渠道：alipay/wechat/bank")
    @TableField("pay_channel")
    private String payChannel;

    @Schema(description = "第三方交易流水号")
    @TableField("transaction_no")
    private String transactionNo;

    @Schema(description = "支付时间")
    @TableField("pay_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "状态：1待支付，2已支付，3已取消，4已退款")
    @TableField("status")
    private Integer status;

    @Schema(description = "取消时间")
    @TableField("cancel_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;

    @Schema(description = "退款金额（元）")
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @Schema(description = "退款时间")
    @TableField("refund_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date refundTime;

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

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
