package com.homi.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileResourceBizTypeEnum {
    /**
     * 使用表名来关联类型
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/19 21:26
     */
    ROOM("room", "房间图片"),
    HOUSE("house", "房源图片"),
    FOCUS("focus", "集中式");

    private final String code;

    private final String desc;
}
