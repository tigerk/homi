package com.homi.model.dto.contract;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/12
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractTemplateQueryDTO extends PageDTO {
    @Schema(description = "合同模板 ID")
    private Long id;

    @Schema(description = "合同类型")
    private Integer contractType;

    @Schema(description = "合同模板名称")
    private String templateName;

    @Schema(description = "合同状态")
    private Integer status;

}
