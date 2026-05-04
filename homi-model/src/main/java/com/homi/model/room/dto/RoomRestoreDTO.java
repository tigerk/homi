package com.homi.model.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "房间恢复DTO")
public class RoomRestoreDTO {
    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "恢复原因")
    private String restoreReason;

    @Schema(description = "操作人ID")
    private Long updateBy;
}
