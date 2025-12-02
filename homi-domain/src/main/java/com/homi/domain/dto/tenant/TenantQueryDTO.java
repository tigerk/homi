package com.homi.domain.dto.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租客查询数据传输对象
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/02
 */
@Data
public class TenantQueryDTO {
    @Schema(description = "租客姓名", example = "张三")
    private String name;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "身份证号码", example = "123456789012345678")
    private String idNo;

    @Schema(description = "租客类型：1=个人，2=企业", example = "1")
    private Integer tenantType;

    @Schema(description = "租客状态：0=停用，1=启用", example = "1")
    private Integer status;
}
