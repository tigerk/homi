package com.homi.model.finance.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "租客财务流水列表项")
public class FinanceFlowFinanceItemVO {
    @Schema(description = "财务流水ID")
    private Long id;

    @Schema(description = "财务流水号")
    private String flowNo;

    @Schema(description = "支付流水ID")
    private Long paymentFlowId;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务单据ID")
    private Long bizId;

    @Schema(description = "业务单据编号")
    private String bizNo;

    @Schema(description = "流水类型")
    private String flowType;

    @Schema(description = "资金方向")
    private String flowDirection;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "费用类型")
    private String feeType;

    @Schema(description = "费用名称")
    private String feeName;

    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

    @Schema(description = "账单ID")
    private Long billId;

    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "账单期数")
    private Integer sortOrder;

    @Schema(description = "房源地址")
    private String roomAddress;

    @Schema(description = "支付流水号")
    private String paymentNo;

    @Schema(description = "支付方式")
    private String paymentChannel;

    @Schema(description = "支付流水审批状态")
    private Integer paymentApprovalStatus;

    @Schema(description = "支付流水状态")
    private Integer paymentStatus;

    @Schema(description = "交易流水号")
    private String thirdTradeNo;

    @Schema(description = "支付凭证")
    private String paymentVoucherUrl;

    @Schema(description = "付款方姓名")
    private String payerName;

    @Schema(description = "付款方手机号")
    private String payerPhone;

    @Schema(description = "收款方名称")
    private String receiverName;

    @Schema(description = "操作人")
    private String operatorName;

    @Schema(description = "财务流水备注")
    private String remark;

    @Schema(description = "支付备注")
    private String paymentRemark;

    @Schema(description = "账单应收日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "账单开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账单结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "财务流水时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date flowAt;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;
}
