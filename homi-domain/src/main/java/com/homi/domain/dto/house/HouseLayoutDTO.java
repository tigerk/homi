package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class HouseLayoutDTO {
    @Schema(description = "主键id，tmp开头则为临时id")
    private Long id;

    @Schema(description = "房型名称")
    private String layoutName;

    @Schema(description = "厅")
    private Integer livingRoom;

    @Schema(description = "卫")
    private Integer bathroom;

    @Schema(description = "厨")
    private Integer kitchen;

    @Schema(description = "室")
    private Integer bedroom;

    @Schema(description = "设施、从字典dict_data获取并配置")
    private List<FacilityItemDTO> facilities;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "图片列表")
    private List<String> imageList;

    @Schema(description = "视频列表")
    private List<String> videoList;

    @Schema(description = "新创建的")
    private Boolean newly;
}
