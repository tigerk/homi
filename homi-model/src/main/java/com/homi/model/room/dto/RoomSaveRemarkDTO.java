package com.homi.model.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/2/26
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "房间备注记录DTO")
public class RoomSaveRemarkDTO implements Serializable {
    @Schema(description = "房间 ID")
    private Long roomId;

    @Schema(description = "备注内容")
    private String remark;

    @Schema(description = "更新人ID", hidden = true)
    private Long updateBy;
}
