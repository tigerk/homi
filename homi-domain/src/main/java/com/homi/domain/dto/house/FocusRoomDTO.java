package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "集中式房间创建DTO")
public class FocusRoomDTO {
    @Schema(description = "房间id")
    private Long id;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "是否已锁定")
    private Boolean locked;

    @Schema(description = "楼层")
    private Integer floorLevel;
}
