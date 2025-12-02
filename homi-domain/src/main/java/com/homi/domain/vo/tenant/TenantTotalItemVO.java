package com.homi.domain.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantTotalItemVO {
    @Schema(description = "租客合同状态")
    private Integer status;

    @Schema(description = "租客合同状态名称")
    private String statusName;

    @Schema(description = "租客合同状态颜色")
    private String statusColor;

    @Schema(description = "数量")
    private Integer total;
}
