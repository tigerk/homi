package com.homi.model.company.dto.init;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "公司默认字典对象")
public class InitDictDTO {
    @Schema(description = "父级字典ID, 0表示根字典")
    private Long parentId;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "子字典列表")
    private List<InitDictDTO> children;

    /**
     * 字典默认数据
     */
    private List<InitDictDataDTO> dictDataList;
}
