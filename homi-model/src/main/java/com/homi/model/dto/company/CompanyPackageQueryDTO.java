package com.homi.model.dto.company;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/16
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "公司套餐列表查询DTO")
public class CompanyPackageQueryDTO extends PageDTO {
    /**
     * 套餐名称
     */
    private String name;

    /**
     * 状态（1正常，0禁用）
     */
    private Integer status;
}
