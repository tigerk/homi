package com.homi.model.tenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class TenantProfileSearchVO {
    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "租客类型：0=个人，1=企业")
    private Integer tenantType;

    @Schema(description = "展示名称")
    private String tenantName;

    @Schema(description = "展示电话")
    private String tenantPhone;

    @Schema(description = "最近更新时间")
    private Date updateAt;

    @Schema(description = "个人租客资料")
    private TenantPersonalVO tenantPersonal;

    @Schema(description = "企业租客资料")
    private TenantCompanyVO tenantCompany;
}
