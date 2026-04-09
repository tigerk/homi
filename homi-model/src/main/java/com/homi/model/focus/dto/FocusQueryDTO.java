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

    @Schema(description = "搜索关键词")
    private String keywords;

    @Schema(description = "租赁模式: 1=集中式 2=分散式")
    private Integer leaseMode;

    @Schema(description = "租赁模式关联ID")
    private Long leaseModeId;
}
