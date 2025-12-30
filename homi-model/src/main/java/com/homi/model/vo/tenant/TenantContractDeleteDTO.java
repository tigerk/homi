package com.homi.model.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/30
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "租客合同删除DTO")
public class TenantContractDeleteDTO implements Serializable {
    @Schema(description = "租客合同ID")
    private Long tenantContractId;
}
