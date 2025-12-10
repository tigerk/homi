package com.homi.common.lib.enums.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型：image, video, pdf
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/11/5 10:50
 */

@Getter
@AllArgsConstructor
public enum FileTypeEnum {
    IMAGE(0),
    VIDEO(1),
    PDF(2),
    ;

    private final Integer code;
}
