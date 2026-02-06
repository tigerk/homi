package com.homi.model.tenant.vo;

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
@Schema(description = "租客合同签署状态更新DTO")
public class LeaseContractSignStatusUpdateDTO implements Serializable {
    @Schema(description = "租客合同ID")
    private Long leaseContractId;

    @Schema(description = "签署状态")
    private Integer signStatus;
}
