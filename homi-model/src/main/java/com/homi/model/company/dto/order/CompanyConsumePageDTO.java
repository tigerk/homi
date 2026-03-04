package com.homi.model.company.dto.order;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "企业服务使用记录分页查询参数")
public class CompanyConsumePageDTO extends PageDTO {

    @Schema(description = "商品编码")
    private String productCode;
}
