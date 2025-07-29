package com.homi.domain.dto.dict.data;

import com.homi.domain.base.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "字典数据项查询")
public class DictDataQueryDTO extends PageDTO {

    @NotNull(message = "字典ID不能为空")
    private Long dictId;

    private String name;

    private String value;

    private Integer status;
}
