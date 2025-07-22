package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "集中式房型创建DTO")
public class FocusRoomLayoutDTO {
    @Schema(description = "房型id", hidden = true)
    private Long id;

    @Schema(description = "房型名称")
    private String layoutName;

    @Schema(description = "室内面积")
    private BigDecimal insideSpace;

    @Schema(description = "客厅数量")
    private Integer livingRoom;

    @Schema(description = "卫生间数量")
    private Integer bathroom;

    @Schema(description = "厨房数量")
    private Integer kitchen;

    @Schema(description = "卧室数量")
    private Integer bedroom;

    @Schema(description = "租赁价格")
    private BigDecimal leasePrice;

    @Schema(description = "房间号列表")
    private List<String> roomNumbers;
}
