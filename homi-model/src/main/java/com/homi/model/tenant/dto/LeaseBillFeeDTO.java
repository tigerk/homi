package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "租客账单费用DTO")
public class LeaseBillFeeDTO {
    @Schema(description = "费用类型：RENTAL/DEPOSIT/OTHER_FEE")
    private String feeType;

    @Schema(description = "费用字典 ID")
    private Long dictDataId;

    @Schema(description = "费用名称")
    private String name;

    @Schema(description = "费用金额")
    private BigDecimal amount;

    @Schema(description = "费用周期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeStart;

    @Schema(description = "费用周期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date feeEnd;

    @Schema(description = "备注")
    private String remark;
}
