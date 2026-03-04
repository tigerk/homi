package com.homi.model.company.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企业服务订购参数")
public class CompanyOrderCreateDTO {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "备注")
    private String remark;
}
