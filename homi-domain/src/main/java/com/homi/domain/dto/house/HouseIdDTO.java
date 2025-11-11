package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/10
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseIdDTO {
    @Schema(description = "房源ID")
    private Long id;
}
