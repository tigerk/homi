package com.homi.model.finance.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付流水查询DTO")
public class PaymentFlowFinanceQueryDTO extends PageDTO {
    @Schema(description = "支付流水业务类型")
    private String bizType;

    @Schema(description = "支付流水状态：1=待审批，2=支付成功，4=已关闭")
    private Integer status;

    @Schema(description = "对象名称")
    private String tenantName;

    @Schema(description = "对象联系电话")
    private String tenantPhone;

    @Schema(description = "房源/对象/账单关键词")
    private String roomKeyword;
}
