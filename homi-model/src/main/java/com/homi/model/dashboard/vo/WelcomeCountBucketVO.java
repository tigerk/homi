package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "欢迎页数量分桶")
public class WelcomeCountBucketVO {
    @Schema(description = "分桶键")
    private String key;

    @Schema(description = "分桶名称")
    private String label;

    @Schema(description = "数量")
    private Integer count;
}
