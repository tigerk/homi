package com.homi.domain.dto.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantCreateDTO {
    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "租户手机号")
    private String tenantPhone;
}
