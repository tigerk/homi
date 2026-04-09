package com.homi.model.house.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "房源查询DTO")
public class HouseQueryDTO extends PageDTO {

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "搜索关键字，支持房源名称/编号/地址")
    private String keywords;

    @Schema(description = "租赁模式: 1=集中式 2=分散式")
    private Integer leaseMode;

    @Schema(description = "排除的业主合同ID，编辑当前合同场景下用于保留当前合同已选房源")
    private Long excludeOwnerContractId;
}
