package com.homi.model.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "房间删除DTO")
public class RoomDeleteDTO {
    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "删除原因")
    private String deleteReason;

    @Schema(description = "操作人ID")
    private Long updateBy;
}
