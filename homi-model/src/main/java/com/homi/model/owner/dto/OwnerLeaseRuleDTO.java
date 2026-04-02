package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.lease.LeaseRentDueTypeEnum;
import com.homi.common.lib.enums.owner.OwnerProrateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "包租规则DTO")
public class OwnerLeaseRuleDTO {
    @Schema(description = "总月租金")
    private BigDecimal rentAmount;

    @Schema(description = "总押金")
    private BigDecimal depositAmount;

    @Schema(description = "押金月数")
    private Integer depositMonths;

    @Schema(description = "付款月数")
    private Integer paymentMonths;

    @Schema(description = "付款方式文案")
    private String payWay;

    @Schema(description = "收租类型")
    private LeaseRentDueTypeEnum rentDueType;

    @Schema(description = "固定收租日")
    private Integer rentDueDay;

    @Schema(description = "收租偏移天数")
    private Integer rentDueOffsetDays;

    @Schema(description = "首付日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date firstPayDate;

    @Schema(description = "计费开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billingStart;

    @Schema(description = "计费结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billingEnd;

    @Schema(description = "折算方式")
    private OwnerProrateTypeEnum prorateType;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "备注")
    private String remark;
}
