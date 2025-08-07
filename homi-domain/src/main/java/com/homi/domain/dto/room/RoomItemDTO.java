package com.homi.domain.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
public class RoomItemDTO {
    @Schema(description = "房间id")
    private Long roomId;

    @Schema(description = "房源编号")
    private String houseCode;

    @Schema(description = "房源名称")
    private String houseName;

    @Schema(description = "房型")
    private HouseLayoutDTO houseLayout;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "出租价格")
    private BigDecimal leasePrice;

    @Schema(description = "房间状态")
    private String roomStatus;

    @Schema(description = "负责人id")
    private String salesmanId;

    @Schema(description = "负责人姓名")
    private String salesmanName;

    @Schema(description = "负责人手机号")
    private String salesmanPhone;
}
