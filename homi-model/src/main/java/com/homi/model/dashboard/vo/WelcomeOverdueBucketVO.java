package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "欢迎页逾期欠款分桶")
public class WelcomeOverdueBucketVO {
    @Schema(description = "分桶键")
    private String key;

    @Schema(description = "分桶名称")
    private String label;

    @Schema(description = "欠款总额")
    private BigDecimal amount;
}
