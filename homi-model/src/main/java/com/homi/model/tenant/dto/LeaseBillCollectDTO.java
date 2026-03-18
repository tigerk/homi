package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "租客账单收款DTO")
public class LeaseBillCollectDTO {
    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "本次收款总金额")
    private BigDecimal totalAmount;

    @Schema(description = "支付方式：1=现金，2=转账，3=支付宝，4=微信，5=其他")
    private Integer payChannel;

    @Schema(description = "实际支付日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "本次收款分配明细")
    private List<Item> items;

    @Schema(description = "更新人ID")
    private Long updateBy;

    @Data
    @Schema(description = "账单费用项收款明细")
    public static class Item {
        @Schema(description = "账单费用项ID")
        private Long leaseBillFeeId;

        @Schema(description = "本次收款金额")
        private BigDecimal amount;
    }
}
