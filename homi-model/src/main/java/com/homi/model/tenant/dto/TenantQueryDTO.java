package com.homi.model.tenant.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租客查询数据传输对象
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantQueryDTO extends PageDTO {
    @Schema(description = "租客 ID", example = "1")
    private Long tenantId;

    @Schema(description = "租约 ID", example = "1")
    private Long leaseId;

    @Schema(description = "租客姓名", example = "张三")
    private String name;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "租客类型：1=个人，2=企业", example = "1")
    private Integer tenantType;

    @Schema(description = "合同状态：0=待签字，1=在租中，2=已退租，3=已作废", example = "0")
    private Integer status;

    @Schema(description = "房间ID", example = "1")
    private Long roomId;
}
