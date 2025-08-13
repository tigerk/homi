package com.homi.domain.dto.dict.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "字典数据项创建对象")
public class SysDictDataUpdateDTO {
    @Schema(description = "字典数据ID")
    private Long id;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态（1正常 0停用）")
    private Integer status;
}
