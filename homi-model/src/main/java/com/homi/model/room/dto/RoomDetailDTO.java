package com.homi.model.room.dto;

import com.homi.model.house.dto.FacilityItemDTO;
import com.homi.model.room.dto.price.PriceConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "房间创建DTO，包含房间基本信息和价格配置")
public class RoomDetailDTO {
    @Schema(description = "房间id")
    private Long id;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "房间类型")
    private Integer roomType;

    @Schema(description = "面积")
    private BigDecimal area;

    @Schema(description = "朝向")
    private String direction;

    @Schema(description = "出租价格")
    private BigDecimal price;

    @Schema(description = "设施、从字典dict_data获取并配置")
    private List<FacilityItemDTO> facilities;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "图片列表")
    private List<String> imageList;

    @Schema(description = "房间价格配置")
    private PriceConfigDTO priceConfig;
}
