package com.homi.model.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "集中式房间创建DTO")
public class FocusHouseDTO {
    @Schema(description = "房源ID")
    private Long id;

    @Schema(description = "游标")
    private String cursor;

    @Schema(description = "房源索引id")
    private Long houseIndex;

    @Schema(description = "座栋")
    private String building;

    @Schema(description = "单元")
    private String unit;

    @Schema(description = "房间号")
    private String doorNumber;

    @Schema(description = "户型id")
    private Long houseLayoutId;

    @Schema(description = "锁定状态：是否锁定")
    private Boolean locked;

    @Schema(description = "禁用状态：是否已禁用")
    private Boolean closed;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "朝向")
    private String direction;

    @Schema(description = "面积")
    private BigDecimal area;

    @Schema(description = "价格")
    private BigDecimal price;
}
