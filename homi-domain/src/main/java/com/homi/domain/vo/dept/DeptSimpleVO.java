package com.homi.domain.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/30
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "部门simpleVO")
public class DeptSimpleVO {
    private Long id;

    private String name;
}
