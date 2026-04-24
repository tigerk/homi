package com.homi.model.tenant.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租客详情数据VO")
public class TenantDetailVO {
    @Schema(description = "租客 ID")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "租客类型关联ID")
    @TableField("tenant_type_id")
    private Long tenantTypeId;

    @Schema(description = "租客类型：0=个人，1=企业")
    @TableField("tenant_type")
    private Integer tenantType;

    @Schema(description = "租客个人信息")
    private TenantPersonalVO tenantPersonal;

    @Schema(description = "租客企业信息")
    private TenantCompanyVO tenantCompany;

    @Schema(description = "租客名称（冗余字段，便于查询）")
    @TableField("tenant_name")
    private String tenantName;

    @Schema(description = "租客联系电话（冗余字段）")
    @TableField("tenant_phone")
    private String tenantPhone;

    @Schema(description = "租客状态：0=停用，1=正常")
    @TableField("status")
    private Integer status;
}
