package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租客信息单独更新DTO")
public class TenantInfoUpdateDTO {
    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "租客类型：0=个人，1=企业")
    private Integer tenantType;

    @Schema(description = "个人租客信息")
    private TenantPersonalDTO tenantPersonal;

    @Schema(description = "企业租客信息")
    private TenantCompanyDTO tenantCompany;

    @Schema(description = "操作人ID", hidden = true)
    private Long createBy;
}
