package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "集中式楼栋DTO")
public class FocusBuildingDTO {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "集中式ID")
    private Long focusId;

    @Schema(description = "座栋")
    private String building;

    @Schema(description = "单元")
    private String unit;

    @Schema(description = "房号前缀")
    private String housePrefix;

    @Schema(description = "房号长度")
    private Integer numberLength;

    @Schema(description = "去掉4")
    private Boolean excludeFour;

    @Schema(description = "总楼层")
    private Integer floorTotal;

    @Schema(description = "关闭的楼层列表json")
    private List<Integer> closedFloors;
}
