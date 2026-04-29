package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "轻托管免租规则DTO")
public class OwnerRentFreeRuleDTO {
    @Schema(description = "是否启用免租")
    private Boolean enabled;

    @Schema(description = "免租类型")
    private String freeType;

    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    @Schema(description = "承担方式")
    private String bearType;

    @Schema(description = "业主承担比例")
    private BigDecimal ownerRatio;

    @Schema(description = "平台承担比例")
    private BigDecimal platformRatio;

    @Schema(description = "计算方式")
    private String calcMode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
