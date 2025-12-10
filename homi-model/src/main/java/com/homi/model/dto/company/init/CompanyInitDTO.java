package com.homi.model.dto.company.init;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "公司默认对象")
public class CompanyInitDTO {
    private Integer ver;

    /**
     * 字典默认数据
     */
    @Schema(description = "字典默认数据")
    private List<InitDictDTO> dicts;
}
