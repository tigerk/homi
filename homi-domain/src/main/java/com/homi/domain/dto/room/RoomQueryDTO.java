package com.homi.domain.dto.room;

import com.homi.domain.base.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "集中式房源创建DTO")
public class RoomQueryDTO extends PageDTO {
    @Schema(description = "房源租赁类型：1、集中式；2、整租、3、合租")
    private Long leaseMode;

    @Schema(description = "模式引用ID")
    private Long modeRefId;

    @Schema(description = "搜索关键字")
    private String keywords;

    @Schema(description = "房间状态")
    private Integer roomStatus;
}
