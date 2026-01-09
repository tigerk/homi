package com.homi.model.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 domix-platform
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/28
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "菜单")
public class MenuIdDTO {
    @Schema(description = "菜单ID，必填")
    @NotNull(message = "菜单ID不能为空")
    private Long id;
}
