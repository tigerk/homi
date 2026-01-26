package com.homi.model.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/17
 */

@Data
@Builder
@Schema(description = "code-name VO")
@AllArgsConstructor
public class CodeNameVO {

    /**
     * 标签
     */
    private String code;

    /**
     * 值
     */
    private String name;
}
