package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "租客账单收款DTO")
public class LeaseBillCollectDTO {
    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付")
    private Integer payStatus;

    @Schema(description = "实际支付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付方式：1=现金，2=转账，3=支付宝，4=微信，5=其他")
    private Integer payChannel;

    @Schema(description = "实际支付日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "更新人ID")
    private Long updateBy;
}
