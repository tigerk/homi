package com.homi.domain.dto.room;

import com.homi.domain.base.PageDTO;
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

    @Schema(description = "小区ID列表")
    private List<Long> communityIds;

    @Schema(description = "楼栋ID列表")
    private List<String> buildings;

    @Schema(description = "单元ID列表")
    private List<String> units;

    @Schema(description = "楼层ID列表")
    private List<Integer> floors;

    @Schema(description = "房源租赁类型：1、集中式；2、整租、3、合租")
    private Integer leaseMode;

    @Schema(description = "模式引用ID")
    private Long modeRefId;

    @Schema(description = "搜索关键字")
    private String keywords;

    @Schema(description = "房间状态")
    private Integer roomStatus;
}
