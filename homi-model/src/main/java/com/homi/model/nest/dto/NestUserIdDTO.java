package com.homi.model.nest.dto;

import lombok.Data;

/**
 * 应用于 domix-platform
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/25
 */

@Data
public class NestUserIdDTO {
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
