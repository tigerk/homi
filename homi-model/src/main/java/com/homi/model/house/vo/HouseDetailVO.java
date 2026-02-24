package com.homi.model.house.vo;

import com.homi.model.community.dto.CommunityDTO;
import com.homi.model.house.dto.HouseLayoutDTO;
import com.homi.model.room.dto.RoomCreateDTO;
import com.homi.model.room.vo.RoomDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/10
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "房源详情")
public class HouseDetailVO {
    @Schema(description = "房源ID")
    private Long id;

    @Schema(description = "房源编号")
    private String houseCode;

    @Schema(description = "房源名称")
    private String houseName;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "业务员ID")
    private Long salesmanId;

    @Schema(description = "业务员")
    private String salesmanName;

    @Schema(description = "房源租赁类型：1、集中式；2、分散式")
    private Integer leaseMode;

    @Schema(description = "来源id，集中式为集中式id，整租、合租为community_id")
    private Long leaseModeId;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "座栋")
    private String building;

    @Schema(description = "单元")
    private String unit;

    @Schema(description = "门牌号")
    private String doorNumber;

    @Schema(description = "户型")
    private Long houseLayoutId;

    @Schema(description = "出租类型：1=整租，2=合租")
    private Integer rentalType;

    @Schema(description = "套内面积")
    private BigDecimal area;

    @Schema(description = "朝向")
    private String direction;

    @Schema(description = "装修类型：1=豪华装，2=简装，3=精装，4=毛坯，5=清水，6=简约，7=未装修")
    private Integer decorationType;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "总楼层")
    private Integer floorTotal;

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

    @Schema(description = "物业费，每月")
    private BigDecimal propertyFee;

    @Schema(description = "暖气费，每月")
    private BigDecimal heatingFee;

    @Schema(description = "物业费，每月")
    private BigDecimal mgmtFee;

    @Schema(description = "房间数 为0表示未分配房间")
    private Integer roomCount;

    @Schema(description = "房间余量")
    private Integer restRoomCount;

    @Schema(description = "权属证明及编号")
    private String certificateNo;

    @Schema(description = "是否共有产权  0=否 1=是")
    private Boolean sharedOwner;

    @Schema(description = "是否抵押  0=否 1=是")
    private Boolean mortgaged;

    @Schema(description = "客户Id")
    private Long customerId;

    @Schema(description = "房源状态")
    private Integer houseStatus;

    @Schema(description = "锁定状态：是否锁定")
    private Boolean locked;

    @Schema(description = "禁用状态：是否已禁用")
    private Boolean closed;

    @Schema(description = "房源描述、项目介绍")
    private String houseDesc;

    @Schema(description = "商圈介绍、广告语")
    private String businessDesc;

    @Schema(description = "备注")
    private String remark;

    /*
     * 以下是结构化的小区、户型、房间列表数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 10:09
     */

    @Schema(description = "住宅小区")
    private CommunityDTO community;

    @Schema(description = "户型，保存合租房源的公共图片、房源配置、图片等信息")
    private HouseLayoutDTO houseLayout;

    /*
     * 合租使用：房间列表，每个房间包含房间号、面积、价格等信息
     */
    @Schema(description = "合租使用：房间列表，每个房间包含房间号、面积、价格等信息")
    private List<RoomDetailVO> roomList;
}
