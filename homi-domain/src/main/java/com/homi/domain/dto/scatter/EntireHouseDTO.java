package com.homi.domain.dto.scatter;

import com.homi.domain.dto.house.FacilityItemDTO;
import com.homi.domain.dto.house.HouseLayoutDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "分散式房间创建DTO")
public class EntireHouseDTO {
    @Schema(description = "房源ID")
    private Long id;

    @Schema(description = "房源编号")
    private String houseCode;

    @Schema(description = "出租类型：1=整租，2=合租")
    private Integer rentalType;

    @Schema(description = "座栋")
    private String building;

    @Schema(description = "单元")
    private String unit;

    @Schema(description = "门牌号")
    private String doorNumber;

    @Schema(description = "户型")
    private HouseLayoutDTO houseLayout;

    @Schema(description = "锁定状态：是否锁定")
    private Boolean locked;

    @Schema(description = "禁用状态：是否已禁用")
    private Boolean closed;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "总楼层")
    private Integer floorTotal;

    @Schema(description = "朝向")
    private String direction;

    @Schema(description = "面积")
    private BigDecimal area;

    @Schema(description = "价格")
    private BigDecimal price;
}
