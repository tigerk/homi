package com.homi.model.finance.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "租客账单财务页查询DTO")
public class LeaseBillFinanceQueryDTO extends PageDTO {
    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付")
    private Integer payStatus;

    @Schema(description = "是否仅查询逾期账单")
    private Boolean overdueOnly;

    @Schema(description = "未来几天内应收")
    private Integer dueWithinDays;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

    @Schema(description = "房源信息关键词")
    private String roomKeyword;
}
