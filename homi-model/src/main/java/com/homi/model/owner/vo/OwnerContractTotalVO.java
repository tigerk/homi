package com.homi.model.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "业主合同统计VO")
public class OwnerContractTotalVO {
    @Schema(description = "全部合同数")
    private Integer total;

    @Schema(description = "待审核合同数")
    private Integer pendingApprovalTotal;

    @Schema(description = "已退房合同数")
    private Integer checkedOutTotal;

    @Schema(description = "已作废合同数")
    private Integer voidedTotal;

    @Schema(description = "待签字合同数")
    private Integer pendingSignTotal;

    @Schema(description = "已签字合同数")
    private Integer signedTotal;

    @Schema(description = "30天内到期合同数")
    private Integer expiring30DaysTotal;
}
