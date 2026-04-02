package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.owner.OwnerFreeCalcModeEnum;
import com.homi.common.lib.enums.owner.OwnerFreeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "包租免租规则DTO")
public class OwnerLeaseFreeRuleDTO {
    @Schema(description = "免租类型")
    private OwnerFreeTypeEnum freeType;

    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    @Schema(description = "计算方式")
    private OwnerFreeCalcModeEnum calcMode;

    @Schema(description = "免租金额")
    private BigDecimal freeAmount;

    @Schema(description = "免租比例")
    private BigDecimal freeRatio;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "备注")
    private String remark;
}
