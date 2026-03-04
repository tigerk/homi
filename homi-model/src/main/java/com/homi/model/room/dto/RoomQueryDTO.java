package com.homi.model.room.dto;

import com.homi.common.lib.dto.PageDTO;
import com.homi.model.room.vo.grid.RoomAggregatedVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "房间查询DTO")  // 原来写的是"集中式房源创建DTO"，描述完全错误
public class RoomQueryDTO extends PageDTO {

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "房间ID列表")
    private List<Long> roomIds;

    @Schema(description = "空间查询参数")
    private List<RoomAggregatedVO> spatialQuery;

    @Schema(description = "房源租赁类型：1=集中式，2=分散式")
    private Integer leaseMode;

    @Schema(description = "模式引用ID")
    private Long leaseModeId;

    @Schema(description = "搜索关键字")
    private String keywords;

    // ========== 房间状态查询（三个独立维度，不可混用）==========
    @Schema(description = "出租占用状态：0=空置，1=已租，2=已预定，3=配置中。locked=true 或 closed=true 时此字段无效")
    private Integer occupancyStatus;

    @Schema(description = "管理锁定状态：true=只查锁定房间。与 occupancyStatus 互斥，locked=true 时忽略 occupancyStatus")
    private Boolean locked;

    @Schema(description = "关闭状态：true=只查已关闭房间。优先级高于 locked 和 occupancyStatus")
    private Boolean closed;
}
