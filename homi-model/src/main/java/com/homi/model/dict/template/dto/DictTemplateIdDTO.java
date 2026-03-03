package com.homi.model.dict.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "模板ID参数")
public class DictTemplateIdDTO {
    @Schema(description = "主键ID")
    private Long id;
}

