package com.homi.model.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同统计VO")
public class OwnerContractTotalVO {
    @Schema(description = "全部合同数")
    private Integer total;

    @Schema(description = "启用中合同数")
    private Integer activeTotal;

    @Schema(description = "已停用合同数")
    private Integer disabledTotal;

    @Schema(description = "待签字合同数")
    private Integer pendingSignTotal;

    @Schema(description = "已签字合同数")
    private Integer signedTotal;

    @Schema(description = "30天内到期合同数")
    private Integer expiring30DaysTotal;
}
