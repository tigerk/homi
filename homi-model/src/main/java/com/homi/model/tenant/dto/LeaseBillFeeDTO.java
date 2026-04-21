package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "租客账单费用DTO")
public class LeaseBillFeeDTO {
    @Schema(description = "费用项ID")
    private Long id;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "费用类型：RENTAL/DEPOSIT/OTHER_FEE")
    private String feeType;

    @Schema(description = "费用字典 ID")
    private Long dictDataId;

    @Schema(description = "费用名称")
    private String feeName;

    @Schema(description = "费用金额")
    private BigDecimal amount;

    @Schema(description = "已收金额")
    private BigDecimal paidAmount;

    @Schema(description = "待收金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付")
    private Integer payStatus;

    @Schema(description = "费用周期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeStartDate;

    @Schema(description = "费用周期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeEndDate;

    @Schema(description = "备注")
    private String remark;
}
