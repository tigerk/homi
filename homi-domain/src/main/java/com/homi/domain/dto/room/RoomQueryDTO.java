package com.homi.domain.dto.room;

import com.homi.domain.base.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "集中式房源创建DTO")
public class RoomQueryDTO extends PageDTO {
    @Schema(description = "项目Id")
    private Long houseId;

    @Schema(description = "运营模式：1集中式 or 2分散式")
    private Long operationMode;

    @Schema(description = "搜索关键字")
    private String keywords;

    @Schema(description = "房间状态")
    private Integer roomStatus;
}
