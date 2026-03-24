package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "欢迎页逾期租客")
public class WelcomeOverdueTenantVO {
    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "租客名称")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

    @Schema(description = "逾期欠款金额")
    private BigDecimal unpaidAmount;
}
