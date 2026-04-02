package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.owner.OwnerBearTypeEnum;
import com.homi.common.lib.enums.owner.OwnerFeeModeEnum;
import com.homi.common.lib.enums.owner.OwnerIncomeBasisEnum;
import com.homi.common.lib.enums.owner.OwnerSettlementModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "轻托管结算规则DTO")
public class OwnerSettlementRuleDTO {
    @Schema(description = "收入口径")
    private OwnerIncomeBasisEnum incomeBasis;

    @Schema(description = "结算模式")
    private OwnerSettlementModeEnum settlementMode;

    @Schema(description = "保底租金")
    private BigDecimal guaranteedRentAmount;

    @Schema(description = "佣金方式")
    private OwnerFeeModeEnum commissionMode;

    @Schema(description = "佣金值")
    private BigDecimal commissionValue;

    @Schema(description = "服务费方式")
    private OwnerFeeModeEnum serviceFeeMode;

    @Schema(description = "服务费值")
    private BigDecimal serviceFeeValue;

    @Schema(description = "税费承担方式")
    private OwnerBearTypeEnum bearTaxType;

    @Schema(description = "生效开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date effectiveStart;

    @Schema(description = "生效结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date effectiveEnd;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "备注")
    private String remark;
}
