package com.homi.model.focus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Schema(description = "集中式项目统计信息")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FocusTotalVO implements Serializable {
    @Schema(description = "总房间数量")
    private Long totalRoomCount;

    @Schema(description = "已出租房间数量")
    private Long totalRentedRoomCount;

    @Schema(description = "出租率")
    private Double occupancyRate;
}
