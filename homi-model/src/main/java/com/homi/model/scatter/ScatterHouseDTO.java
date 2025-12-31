package com.homi.model.scatter;

import com.homi.model.dto.house.HouseLayoutDTO;
import com.homi.model.dto.room.RoomDetailDTO;
import com.homi.model.dto.room.price.PriceConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "分散式房源DTO")
public class ScatterHouseDTO {
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

    @Schema(description = "户型，保存合租房源的公共图片、房源配置、图片等信息")
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

    /*
     * 整租使用：price + priceConfig 直接在 house 中配置。
     */
    @Schema(description = "整租使用：房间出租价格，单位：元/月")
    private BigDecimal price;

    @Schema(description = "整租使用：房间价格配置")
    private PriceConfigDTO priceConfig;

    /*
     * 合租使用：房间列表，每个房间包含房间号、面积、价格等信息
     */
    @Schema(description = "合租使用：房间列表，每个房间包含房间号、面积、价格等信息")
    private List<RoomDetailDTO> roomList;
}
