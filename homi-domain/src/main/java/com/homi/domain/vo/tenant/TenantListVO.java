package com.homi.domain.vo.tenant;

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
public class TenantListVO {
    @Schema(description = "房间id")
    private Long id;

}
