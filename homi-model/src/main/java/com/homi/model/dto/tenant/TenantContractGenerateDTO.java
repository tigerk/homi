package com.homi.model.dto.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租客查询数据传输对象
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/02
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "租客合同生成 DTO")
public class TenantContractGenerateDTO {
    @Schema(description = "租客合同 ID", example = "1")
    private Long tenantContractId;

    @Schema(description = "租客 ID", example = "1")
    private Long tenantId;

    @Schema(description = "合同模板 ID", example = "1")
    private Long contractTemplateId;
}
