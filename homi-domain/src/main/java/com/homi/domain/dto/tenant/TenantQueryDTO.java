package com.homi.domain.dto.tenant;

import com.homi.domain.base.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TenantQueryDTO extends PageDTO {
    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "租户手机号")
    private String tenantPhone;
}
