package com.homi.model.dict.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典数据模板查询参数")
public class DictDataTemplateQueryDTO {
    @Schema(description = "字典编码")
    private String dictCode;
}

