package com.homi.model.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "公司套餐创建请求")
public class CompanyPackageIdDTO {
    @Schema(description = "id，修改时需要传")
    private Long id;
}
