package com.homi.model.vo.tenant.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/28
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "租客账单其他费用VO")
public class TenantBillOtherFeeVO implements Serializable {
    @Schema(description = "账单ID（关联 tenant_bill.id）")
    private Long billId;

    @Schema(description = "费用字典 ID")
    private Long dictDataId;

    @Schema(description = "费用项目名称（如 租金、水费、电费）")
    private String name;

    @Schema(description = "费用金额（计算结果）")
    private BigDecimal amount;

    @Schema(description = "备注")
    private String remark;
}
