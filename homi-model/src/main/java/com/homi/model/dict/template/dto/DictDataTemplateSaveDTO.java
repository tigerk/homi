package com.homi.model.dict.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典数据模板保存参数")
public class DictDataTemplateSaveDTO {
    @Schema(description = "主键ID，修改时传")
    private Long id;

    @Schema(description = "归属字典编码")
    private String dictCode;

    @Schema(description = "数据项名称")
    private String name;

    @Schema(description = "数据项值")
    private String value;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "颜色值")
    private String color;

    @Schema(description = "状态（1开启 0关闭）")
    private Integer status;

    @Schema(description = "是否可删除（1可删除 0不可删除）")
    private Boolean deletable;

    @Schema(description = "模板项是否启用")
    private Boolean enabled;

    @Schema(description = "模板版本号")
    private Integer ver;

    @Schema(description = "备注")
    private String remark;
}

