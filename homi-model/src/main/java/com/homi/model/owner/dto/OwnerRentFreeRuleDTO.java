package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.owner.OwnerBearTypeEnum;
import com.homi.common.lib.enums.owner.OwnerFreeCalcModeEnum;
import com.homi.common.lib.enums.owner.OwnerFreeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "轻托管免租规则DTO")
public class OwnerRentFreeRuleDTO {
    @Schema(description = "免租类型")
    private OwnerFreeTypeEnum freeType;

    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    @Schema(description = "承担方式")
    private OwnerBearTypeEnum bearType;

    @Schema(description = "业主承担比例")
    private BigDecimal ownerRatio;

    @Schema(description = "平台承担比例")
    private BigDecimal platformRatio;

    @Schema(description = "计算方式")
    private OwnerFreeCalcModeEnum calcMode;

    @Schema(description = "状态")
    private StatusEnum status;

    @Schema(description = "备注")
    private String remark;
}
