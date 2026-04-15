package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "包租业主应付单明细DTO")
public class OwnerPayableBillLineDTO {
    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "项目类型")
    private String itemType;

    @Schema(description = "项目名称")
    private String itemName;

    @Schema(description = "方向")
    private String direction;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "业务时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date bizDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "公式快照")
    private String formulaSnapshot;
}
