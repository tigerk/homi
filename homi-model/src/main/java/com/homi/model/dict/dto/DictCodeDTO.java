package com.homi.model.dict.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "字典数据项查询")
public class DictCodeDTO extends PageDTO {

    @NotNull(message = "字典编码不能为空")
    private String dictCode;

}
