package com.homi.domain.vo.house;

import com.homi.domain.dto.community.CommunityDTO;
import com.homi.domain.dto.house.HouseLayoutDTO;
import com.homi.domain.dto.room.RoomDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "分散式房源VO")
public class ScatterHouseVO {
    @Schema(description = "房源ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "房源租赁类型：1、集d中式；2、分散式")
    private Integer leaseMode;

    @Schema(description = "住宅小区")
    private CommunityDTO community;

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

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "业务员ID")
    private Long salesmanId;

    @Schema(description = "水")
    private String water;

    @Schema(description = "电")
    private String electricity;

    @Schema(description = "供暖")
    private String heating;

    @Schema(description = "是否有电梯")
    private Boolean hasElevator;

    @Schema(description = "是否有燃气")
    private Boolean hasGas;

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
     * 合租使用：房间列表，每个房间包含房间号、面积、价格等信息
     */
    @Schema(description = "合租使用：房间列表，每个房间包含房间号、面积、价格等信息")
    private List<RoomDetailDTO> roomList;
}
