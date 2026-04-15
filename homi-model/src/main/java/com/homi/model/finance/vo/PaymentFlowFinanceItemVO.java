package com.homi.model.finance.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.homi.model.tenant.vo.bill.FinanceFlowVO;

@Data
@Schema(description = "租客支付流水列表项")
public class PaymentFlowFinanceItemVO {
    @Schema(description = "支付流水ID")
    private Long id;

    @Schema(description = "支付流水号")
    private String paymentNo;

    @Schema(description = "账单ID")
    private Long billId;

    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "账单期数")
    private Integer sortOrder;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

    @Schema(description = "房源信息")
    private String roomAddress;

    @Schema(description = "付款人姓名")
    private String payerName;

    @Schema(description = "付款人电话")
    private String payerPhone;

    @Schema(description = "支付方式")
    private String channel;

    @Schema(description = "支付流水状态")
    private Integer status;

    @Schema(description = "审批状态")
    private Integer approvalStatus;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "交易流水号")
    private String thirdTradeNo;

    @Schema(description = "支付凭证")
    private String paymentVoucherUrl;

    @Schema(description = "支付备注")
    private String remark;

    @Schema(description = "操作人")
    private String operatorName;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "账单应收日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "账单开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账单结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "关联财务流水")
    private List<FinanceFlowVO> financeFlowList;
}
