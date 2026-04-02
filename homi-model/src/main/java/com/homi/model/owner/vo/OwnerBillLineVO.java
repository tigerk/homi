package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "业主账单明细VO")
public class OwnerBillLineVO {
    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "明细类型")
    private String itemType;

    @Schema(description = "明细名称")
    private String itemName;

    @Schema(description = "方向")
    private String direction;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "业务日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date bizDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "公式快照")
    private String formulaSnapshot;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
