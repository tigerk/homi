package com.homi.model.room.vo.grid;

import com.homi.model.room.vo.RoomListVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "房间网格视图对象，用于展示房源的房间网格信息")
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
    @Schema(description = "楼层网格视图对象")
    public static class HouseFloorGridDTO {
        @Schema(description = "房间总数")
        private Long total;

        @Schema(description = "已租率")
        private BigDecimal leasedRate;

        @Schema(description = "楼层")
        private Integer floor;

        @Schema(description = "房间列表")
        private List<RoomListVO> roomList;

    }
}
