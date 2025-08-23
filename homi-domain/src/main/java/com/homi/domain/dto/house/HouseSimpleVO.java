package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/8
 */
@Data
@Schema(description = "房源简略信息")
public class HouseSimpleVO {
    private Long id;

    @Schema(description = "房源编号")
    private String houseCode;

    @Schema(description = "房源名称")
    private String houseName;

}
