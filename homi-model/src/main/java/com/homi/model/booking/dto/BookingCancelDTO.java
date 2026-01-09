package com.homi.model.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/9
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCancelDTO implements Serializable {
    @Schema(description = "预定 ID")
    private Long id;

    @Schema(description = "更新人 ID", hidden = true)
    private Long updateBy;

    @Schema(description = "取消/过期原因备注")
    private String cancelReason;
}
