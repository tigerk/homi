package com.homi.domain.dto.tenant;

import com.homi.domain.base.PageDTO;
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
    private String tenantName;
}
