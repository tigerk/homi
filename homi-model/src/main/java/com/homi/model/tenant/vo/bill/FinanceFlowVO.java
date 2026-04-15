package com.homi.model.tenant.vo.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "财务流水简要信息")
public class FinanceFlowVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "财务流水号")
    private String flowNo;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务单据ID")
    private Long bizId;

    @Schema(description = "业务单据编号")
    private String bizNo;

    @Schema(description = "费用类型")
    private String feeType;

    @Schema(description = "费用名称")
    private String feeName;

    @Schema(description = "流水类型")
    private String flowType;

    @Schema(description = "资金方向")
    private String flowDirection;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "状态：0=入账中、1=已入账、2=失败、3=已作废")
    private Integer status;

    @Schema(description = "退款关联原始流水ID")
    private Long refundFlowId;

    @Schema(description = "流水发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date flowAt;

    @Schema(description = "付款方姓名")
    private String payerName;

    @Schema(description = "付款方手机号")
    private String payerPhone;

    @Schema(description = "收款方名称")
    private String receiverName;

    @Schema(description = "操作员工姓名")
    private String operatorName;

    @Schema(description = "备注")
    private String remark;
}
