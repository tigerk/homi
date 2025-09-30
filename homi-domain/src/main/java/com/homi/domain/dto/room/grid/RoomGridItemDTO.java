package com.homi.domain.dto.room.grid;

import com.homi.domain.dto.room.RoomItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * @date 2025/9/26
 */
@Data
@Schema(description = "房间网格 DTO，用于展示小区楼栋楼层及房间信息")
public class RoomGridItemDTO {
    @Schema(description = "小区信息")
    private CommunityGroup communityGroup;

    @Schema(description = "楼栋单元")
    private UnitGroup unitGroup;

    @Schema(description = "楼层")
    private FloorGroup floorGroup;

    @Schema(description = "房间列表")
    private List<RoomItemDTO> rooms;
}