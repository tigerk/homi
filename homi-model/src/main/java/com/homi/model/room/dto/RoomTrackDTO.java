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
@Schema(description = "房间跟进记录DTO")
public class RoomTrackDTO implements Serializable {
    @Schema(description = "公司ID", hidden = true)
    private Long companyId;

    private Long roomId;

    @Schema(description = "跟进记录")
    private String trackContent;

    @Schema(description = "更新人ID", hidden = true)
    private Long updateBy;
}
