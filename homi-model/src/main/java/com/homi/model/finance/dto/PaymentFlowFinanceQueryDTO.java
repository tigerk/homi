package com.homi.model.finance.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "租客支付流水查询DTO")
public class PaymentFlowFinanceQueryDTO extends PageDTO {
    @Schema(description = "支付流水状态：1=待审批，2=支付成功，4=已关闭")
    private Integer status;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

    @Schema(description = "房源信息关键词")
    private String roomKeyword;
}
