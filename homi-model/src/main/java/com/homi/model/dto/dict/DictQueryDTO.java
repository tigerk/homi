package com.homi.model.dto.dict;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "字典查询对象")
public class DictQueryDTO extends PageDTO {

    private String dictName;

    private String dictCode;

    private Integer status;
}
