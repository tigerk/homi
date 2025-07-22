package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 房源表
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("house")
@Schema(name = "House", description = "房源表")
public class House implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "房源id")
    @TableId("id")
    private Long id;

    @Schema(description = "房源编号")
    @TableField("house_code")
    private String houseCode;

    @Schema(description = "房源名称")
    @TableField("house_name")
    private String houseName;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @TableField("dept_id")
    private Long deptId;

    @TableField("business_mode")
    private Integer businessMode;

    @Schema(description = "业务类型")
    @TableField("product_type")
    private Integer productType;

    @Schema(description = "物业id")
    @TableField("property_id")
    private Long propertyId;

    @Schema(description = "楼盘简称")
    @TableField("property_alias")
    private String propertyAlias;

    @Schema(description = "房本地址")
    @TableField("address")
    private String address;

    @TableField("tags")
    private String tags;

    @Schema(description = "经度")
    @TableField("lng")
    private String lng;

    @Schema(description = "纬度")
    @TableField("lat")
    private String lat;

    @Schema(description = "座栋")
    @TableField("building")
    private String building;

    @Schema(description = "单元")
    @TableField("unit")
    private String unit;

    @Schema(description = "门牌号")
    @TableField("door_number")
    private String doorNumber;

    @Schema(description = "建筑面积")
    @TableField("building_space")
    private BigDecimal buildingSpace;

    @Schema(description = "套内面积")
    @TableField("inside_space")
    private BigDecimal insideSpace;

    @Schema(description = "厅")
    @TableField("living_room")
    private Integer livingRoom;

    @Schema(description = "卫")
    @TableField("bathroom")
    private Integer bathroom;

    @Schema(description = "厨")
    @TableField("kitchen")
    private Integer kitchen;

    @Schema(description = "室")
    @TableField("bedroom")
    private Integer bedroom;

    @Schema(description = "楼层")
    @TableField("floor_level")
    private Integer floorLevel;

    @Schema(description = "总楼层")
    @TableField("floor_total")
    private Integer floorTotal;

    @Schema(description = "朝向")
    @TableField("orientation")
    private Integer orientation;

    @TableField("has_elevator")
    private Boolean hasElevator;

    @TableField("has_gas")
    private Boolean hasGas;

    @Schema(description = "房间数 为0表示未分配房间")
    @TableField("room_count")
    private Integer roomCount;

    @Schema(description = "房间余量")
    @TableField("rest_room_count")
    private Integer restRoomCount;

    @Schema(description = "权属证明及编号")
    @TableField("certificate_no")
    private String certificateNo;

    @Schema(description = "是否共有产权  0=否 1=是")
    @TableField("shared_owner")
    private Boolean sharedOwner;

    @Schema(description = "是否抵押  0=否 1=是")
    @TableField("mortgaged")
    private Boolean mortgaged;

    @Schema(description = "暖气费")
    @TableField("heating_fee")
    private BigDecimal heatingFee;

    @Schema(description = "管理费用：物业费")
    @TableField("management_fee")
    private BigDecimal managementFee;

    @Schema(description = "客户Id")
    @TableField("customer_id")
    private Long customerId;

    @TableField("remark")
    private String remark;

    @TableField("shelf_status")
    private Integer shelfStatus;

    @TableField("house_status")
    private Integer houseStatus;

    @TableField("locked")
    private Boolean locked;

    @TableField("salesman_id")
    private Long salesmanId;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
