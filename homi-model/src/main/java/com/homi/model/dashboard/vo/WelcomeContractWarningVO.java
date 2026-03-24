package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "欢迎页合同与应收预警")
public class WelcomeContractWarningVO {
    @Schema(description = "未来7天应收金额")
    private BigDecimal next7DaysReceivableAmount;

    @Schema(description = "7天内到期合同数")
    private Integer expiring7DaysCount;

    @Schema(description = "30天内到期合同数")
    private Integer expiring30DaysCount;
}
