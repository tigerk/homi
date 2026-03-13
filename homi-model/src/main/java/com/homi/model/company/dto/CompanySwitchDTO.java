package com.homi.model.company.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "公司查询对象")
public class CompanySwitchDTO extends PageDTO {

    @Schema(description = "公司ID")
    private Long companyId;
}
