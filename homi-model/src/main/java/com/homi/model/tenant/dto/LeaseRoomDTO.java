package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "租约房间配置")
public class LeaseRoomDTO {
    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间租金")
    private BigDecimal rentPrice;
}
