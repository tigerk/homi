package com.homi.model.tenant.dto;

import com.homi.model.tenant.vo.LeaseDetailVO;
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
public class LeaseContractGenerateDTO {
    @Schema(description = "租客合同 ID", example = "1")
    private Long leaseContractId;

    @Schema(description = "租约 ID", example = "1")
    private Long leaseId;

    @Schema(description = "租约详情", example = "LeaseDetailVO")
    private LeaseDetailVO leaseDetailVO;

    @Schema(description = "合同模板 ID", example = "1")
    private Long contractTemplateId;
}
