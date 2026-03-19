package com.homi.model.finance.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "租客账单财务页-账单明细列表项")
public class LeaseBillFeeFinanceItemVO {
    @Schema(description = "账单明细ID")
    private Long id;

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

    @Schema(description = "房源地址")
    private String roomAddress;

    @Schema(description = "费用类型")
    private String feeType;

    @Schema(description = "费用名称")
    private String feeName;

    @Schema(description = "应收金额")
    private BigDecimal amount;

    @Schema(description = "已收金额")
    private BigDecimal paidAmount;

    @Schema(description = "待收金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "是否逾期")
    private Boolean overdue;

    @Schema(description = "费用周期开始")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeStart;

    @Schema(description = "费用周期结束")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeEnd;

    @Schema(description = "账单应收日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "备注")
    private String remark;
}
