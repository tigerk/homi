package com.homi.model.room.dto;

import com.homi.common.lib.dto.PageDTO;
import com.homi.model.room.vo.grid.RoomAggregatedVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "集中式房源创建DTO")
public class RoomQueryDTO extends PageDTO {
    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "房间ID列表")
    private List<Long> roomIds;

    @Schema(description = "空间查询参数")
    private List<RoomAggregatedVO> spatialQuery;

    @Schema(description = "房源租赁类型：1、集中式；2、分散式")
    private Integer leaseMode;

    @Schema(description = "模式引用ID")
    private Long leaseModeId;

    @Schema(description = "搜索关键字")
    private String keywords;

    @Schema(description = "房间状态")
    private Integer roomStatus;
}
