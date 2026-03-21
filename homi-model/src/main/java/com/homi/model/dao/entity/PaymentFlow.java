package com.homi.model.dao.entity;

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

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("payment_flow")
@Schema(name = "PaymentFlow", description = "支付流水表（渠道层）")
public class PaymentFlow implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "系统支付流水号")
    @TableField("payment_no")
    private String paymentNo;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业务类型")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "业务单据ID")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "支付渠道")
    @TableField("channel")
    private String channel;

    @Schema(description = "渠道收款账户")
    @TableField("channel_account")
    private String channelAccount;

    @Schema(description = "第三方支付平台交易号")
    @TableField("third_trade_no")
    private String thirdTradeNo;

    @Schema(description = "支付凭证图片")
    @TableField("payment_voucher_url")
    private String paymentVoucherUrl;

    @Schema(description = "第三方平台原始状态")
    @TableField("third_status")
    private String thirdStatus;

    @Schema(description = "金额（分）")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "币种")
    @TableField("currency")
    private String currency;

    @Schema(description = "已退款金额（分）")
    @TableField("refunded_amount")
    private BigDecimal refundedAmount;

    @Schema(description = "资金方向")
    @TableField("flow_direction")
    private String flowDirection;

    @Schema(description = "状态：0=待支付、1=待审批、2=支付成功、3=支付失败、4=已关闭、5=退款中、6=已退款")
    @TableField("status")
    private Integer status;

    @Schema(description = "审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回")
    @TableField("approval_status")
    private Integer approvalStatus;

    @Schema(description = "实际支付完成时间")
    @TableField("pay_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "支付超时时间")
    @TableField("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    @Schema(description = "付款方姓名")
    @TableField("payer_name")
    private String payerName;

    @Schema(description = "付款方手机号")
    @TableField("payer_phone")
    private String payerPhone;

    @Schema(description = "付款方账号")
    @TableField("payer_account")
    private String payerAccount;

    @Schema(description = "收款方名称")
    @TableField("receiver_name")
    private String receiverName;

    @Schema(description = "收款方账号")
    @TableField("receiver_account")
    private String receiverAccount;

    @Schema(description = "操作员工ID")
    @TableField("operator_id")
    private Long operatorId;

    @Schema(description = "操作员工姓名")
    @TableField("operator_name")
    private String operatorName;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "扩展字段")
    @TableField("ext_json")
    private String extJson;

    @Schema(description = "是否删除：0 否，1 是")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;
}
