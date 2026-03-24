package com.homi.model.finance.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "租客账单财务页-账单列表项")
public class LeaseBillFinanceItemVO {
    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "账单期数")
    private Integer sortOrder;

    @Schema(description = "账单类型")
    private Integer billType;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

    @Schema(description = "房源地址")
    private String roomAddress;

    @Schema(description = "账单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "已收金额")
    private BigDecimal paidAmount;

    @Schema(description = "待收金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "账单状态")
    private Integer status;

    @Schema(description = "是否逾期")
    private Boolean overdue;

    @Schema(description = "账单周期开始")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账单周期结束")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "应收日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
