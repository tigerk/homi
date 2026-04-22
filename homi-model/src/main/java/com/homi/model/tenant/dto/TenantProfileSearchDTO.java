package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TenantProfileSearchDTO {
    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "租客类型：0=个人，1=企业")
    private Integer tenantType;

    @Schema(description = "返回数量，默认 10")
    private Integer limit;
}
