package com.homi.domain.vo.room;

import com.homi.domain.dto.room.RoomItemDTO;
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
public class RoomGridVO {
    private Long houseId;

    @Schema(description = "房源编号")
    private String houseCode;

    @Schema(description = "房源名称")
    private String houseName;

    @Schema(description = "房间总数")
    private Long total;

    @Schema(description = "已租率")
    private BigDecimal leasedRate;

    @Schema(description = "楼层列表")
    private List<HouseFloorGridDTO> floorList;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HouseFloorGridDTO {
        @Schema(description = "房间总数")
        private Long total;

        @Schema(description = "已租率")
        private BigDecimal leasedRate;

        @Schema(description = "楼层")
        private Integer floor;

        @Schema(description = "房间列表")
        private List<RoomItemDTO> roomList;

    }
}
