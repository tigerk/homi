package com.homi.model.dict.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典模板保存参数")
public class DictTemplateSaveDTO {
    @Schema(description = "主键ID，修改时传")
    private Long id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "父字典编码，0为根")
    private String parentCode;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态（1开启 0关闭）")
    private Integer status;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "模板项是否启用")
    private Boolean enabled;

    @Schema(description = "模板版本号")
    private Integer ver;

    @Schema(description = "备注")
    private String remark;
}

