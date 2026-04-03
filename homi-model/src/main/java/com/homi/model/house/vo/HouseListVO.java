package com.homi.model.house.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "房源列表VO")
public class HouseListVO {

    @Schema(description = "房源ID")
    private Long houseId;

    @Schema(description = "房源名称")
    private String houseName;

    @Schema(description = "房源编号")
    private String houseCode;

    @Schema(description = "房源地址")
    private String addressText;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "楼栋")
    private String building;

    @Schema(description = "单元")
    private String unit;

    @Schema(description = "门牌号")
    private String doorNumber;

    @Schema(description = "出租类型")
    private Integer rentalType;

    @Schema(description = "套内面积")
    private BigDecimal area;

    @Schema(description = "房间数")
    private Integer roomCount;

    @Schema(description = "参考月租金")
    private BigDecimal referenceRentAmount;

    @Schema(description = "户型文案")
    private String layoutText;

    @Schema(description = "权属证明及编号")
    private String certificateNo;
}
