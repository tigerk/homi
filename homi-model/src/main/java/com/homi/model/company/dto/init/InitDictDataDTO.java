package com.homi.model.company.dto.init;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "公司默认字典对象")
public class InitDictDataDTO {
    @Schema(description = "数据项名称")
    private String name;

    @Schema(description = "数据项值")
    private String value;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "颜色值")
    private String color;
}
