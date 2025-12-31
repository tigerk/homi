package com.homi.model.focus.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "集中式查询 DTO")
public class FocusQueryDTO extends PageDTO {
    /**
     * 公司ID
     */
    @Schema(description = "公司ID")
    private Long companyId;
}
