package com.homi.model.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "房间IDDTO")
public class RoomIdDTO implements Serializable {
    @Schema(description = "房间ID")
    private Long roomId;

}
