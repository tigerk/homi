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
@Schema(description = "租客合同删除DTO")
public class LeaseContractDeleteDTO implements Serializable {
    @Schema(description = "租客签约合同文档ID")
    private Long leaseContractDocId;

    @Schema(description = "租客合同ID，兼容旧字段，等同 leaseContractDocId")
    private Long leaseContractId;
}
