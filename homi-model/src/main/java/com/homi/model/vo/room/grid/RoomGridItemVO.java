package com.homi.model.vo.room.grid;

import com.homi.model.vo.room.RoomListVO;
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
public class RoomGridItemVO {
    @Schema(description = "区域信息，集中式时，为项目信息，分布式时，为小区信息")
    private CompoundGroup compoundGroup;

    @Schema(description = "楼栋单元")
    private BuildingGroup buildingGroup;

    @Schema(description = "楼层")
    private FloorGroup floorGroup;

    @Schema(description = "房间列表")
    private List<RoomListVO> rooms;
}
