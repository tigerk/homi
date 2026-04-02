package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "业主账户流水VO")
public class OwnerAccountFlowVO {
    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "流水方向")
    private String flowDirection;

    @Schema(description = "变动类型")
    private String changeType;

    @Schema(description = "变动金额")
    private BigDecimal amount;

    @Schema(description = "变动前可用金额")
    private BigDecimal availableBefore;

    @Schema(description = "变动后可用金额")
    private BigDecimal availableAfter;

    @Schema(description = "变动前冻结金额")
    private BigDecimal frozenBefore;

    @Schema(description = "变动后冻结金额")
    private BigDecimal frozenAfter;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
