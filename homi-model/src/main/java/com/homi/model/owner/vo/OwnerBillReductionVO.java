package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "业主账单减免明细VO")
public class OwnerBillReductionVO {
    @Schema(description = "减免明细ID")
    private Long id;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "减免类型")
    private String reductionType;

    @Schema(description = "减免名称")
    private String reductionName;

    @Schema(description = "减免金额")
    private BigDecimal amount;

    @Schema(description = "业务日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date bizDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "规则快照")
    private String ruleSnapshot;

    @Schema(description = "状态")
    private Integer status;
}
