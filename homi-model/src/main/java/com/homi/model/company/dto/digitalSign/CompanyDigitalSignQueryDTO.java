package com.homi.model.company.dto.digitalSign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企业电子签章查询DTO")
public class CompanyDigitalSignQueryDTO {
    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "签章类型：1=企业，2=个人")
    private Integer signType;
}
