package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "租客账单更新DTO")
public class LeaseBillUpdateDTO {
    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "账单顺序")
    private Integer sortOrder;

    @Schema(description = "账单类型")
    private Integer billType;

    @Schema(description = "结转来源账单ID")
    private Long carryOverFromBillId;

    @Schema(description = "结转目标账单ID")
    private Long carryOverToBillId;

    @Schema(description = "账单周期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账单周期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "账单合计金额")
    private BigDecimal totalAmount;

    @Schema(description = "应收日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "实际支付日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "实际支付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "支付方式")
    private Integer payChannel;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "是否有效")
    private Boolean valid;

    @Schema(description = "账单费用明细")
    private List<LeaseBillFeeDTO> feeList;
}
