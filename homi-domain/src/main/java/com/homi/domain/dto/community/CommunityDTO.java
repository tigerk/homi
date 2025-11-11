package com.homi.domain.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "住宅小区DTO")
public class CommunityDTO {
    @Schema(description = "小区ID", hidden = true)
    private Long communityId;

    @Schema(description = "小区名称")
    private String name;

    @Schema(description = "城市ID，使用的是区域ID")
    private Long cityId;

    @Schema(description = "行政区划代码")
    private String adcode;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "经纬度，格式：经度,纬度")
    private String location;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
