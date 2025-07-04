package com.homi.domain.vo.dept;

import lombok.Builder;
import lombok.Data;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/30
 */

@Builder
@Data
public class DeptSimpleVO {
    private Long id;

    private String name;
}
