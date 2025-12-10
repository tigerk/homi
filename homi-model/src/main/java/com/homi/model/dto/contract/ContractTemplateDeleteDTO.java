package com.homi.model.dto.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/13
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContractTemplateDeleteDTO {
    @Schema(description = "合同模板ID")
    @NotNull
    private Long id;
}
