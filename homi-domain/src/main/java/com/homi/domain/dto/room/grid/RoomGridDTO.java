package com.homi.domain.dto.room.grid;

import com.homi.domain.vo.room.grid.RoomGridItemVO;
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
public class RoomGridDTO {
    @Schema(description = "房间网格项列表")
    private List<RoomGridItemVO> roomGridItemList;

    @Schema(description = "当前页")
    private Long currentPage;

    @Schema(description = "每页楼层数")
    private Long pageSize;

    @Schema(description = "是否有更多数据")
    private Boolean hasMore;
}