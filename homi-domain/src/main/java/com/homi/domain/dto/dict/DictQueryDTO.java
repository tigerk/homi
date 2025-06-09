package com.homi.domain.dto.dict;

import com.homi.domain.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "字典查询对象")
public class DictQueryDTO extends BasePage {

    private String dictName;

    private String dictCode;

    private Integer status;
}
