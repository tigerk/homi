package com.homi.domain.dto.dict.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "字典数据项创建对象")
public class DictDataCreateDTO {
    @Schema(description = "字典数据ID")
    private Long id;

    /**
     * 字典ID
     */
    @NotNull(message = "字典ID不能为空")
    private Long dictId;

    /**
     * 数据项名称
     */
    @NotBlank(message = "数据项名称不能为空")
    private String name;

    /**
     * 数据项值
     */
    @Schema(description = "数据项值")
    private String value;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 颜色值
     */
    private String color;


    /**
     * 状态（0正常 1停用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;


    /**
     * 备注
     */
    private String remark;
}
