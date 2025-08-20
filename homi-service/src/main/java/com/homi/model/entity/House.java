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
 * @since 2025-08-20
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

    @Schema(description = "区域id")
    @TableField("region_id")
    private Long regionId;

    @Schema(description = "1:集中式/2:分散式")
    @TableField("operation_mode")
    private Integer operationMode;

    @Schema(description = "物业id")
    @TableField("property_id")
    private Long propertyId;

    @Schema(description = "经度")
    @TableField("lng")
    private String lng;

    @Schema(description = "纬度")
    @TableField("lat")
    private String lat;

    @Schema(description = "房本地址")
    @TableField("address")
    private String address;

    @Schema(description = "标签")
    @TableField("tags")
    private String tags;

    @Schema(description = "座栋")
    @TableField("building")
    private String building;

    @Schema(description = "单元")
    @TableField("unit")
    private String unit;

    @Schema(description = "门牌号，分散式独有")
    @TableField("door_number")
    private String doorNumber;

    @Schema(description = "建筑面积")
    @TableField("building_area")
    private BigDecimal buildingArea;

    @Schema(description = "套内面积")
    @TableField("area")
    private BigDecimal area;

    @Schema(description = "楼层")
    @TableField("floor")
    private Integer floor;

    @Schema(description = "总楼层")
    @TableField("floor_total")
    private Integer floorTotal;

    @Schema(description = "水")
    @TableField("water")
    private String water;

    @Schema(description = "电")
    @TableField("electricity")
    private String electricity;

    @Schema(description = "供暖")
    @TableField("heating")
    private String heating;

    @TableField("has_elevator")
    private Boolean hasElevator;

    @TableField("has_gas")
    private Boolean hasGas;

    @Schema(description = "暖气费，每月")
    @TableField("heating_fee")
    private BigDecimal heatingFee;

    @Schema(description = "物业费，每月")
    @TableField("mgmt_fee")
    private BigDecimal mgmtFee;

    @Schema(description = "设施、从字典dict_data获取并配置")
    @TableField("facilities")
    private String facilities;

    @Schema(description = "图片列表")
    @TableField("image_list")
    private String imageList;

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

    @Schema(description = "客户Id")
    @TableField("customer_id")
    private Long customerId;

    @Schema(description = "房源状态")
    @TableField("house_status")
    private Integer houseStatus;

    @Schema(description = "是否锁定")
    @TableField("locked")
    private Boolean locked;

    @Schema(description = "门店联系电话")
    @TableField("store_phone")
    private String storePhone;

    @TableField("salesman_id")
    private Long salesmanId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "房源描述、项目介绍")
    @TableField("house_desc")
    private String houseDesc;

    @Schema(description = "商圈介绍、广告语")
    @TableField("business_desc")
    private String businessDesc;

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
