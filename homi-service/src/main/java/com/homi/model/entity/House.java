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
 * @since 2025-11-12
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

    @Schema(description = "部门ID")
    @TableField("dept_id")
    private Long deptId;

    @Schema(description = "业务员ID")
    @TableField("salesman_id")
    private Long salesmanId;

    @Schema(description = "房源租赁类型：1、集中式；2、整租、3、合租")
    @TableField("lease_mode")
    private Integer leaseMode;

    @Schema(description = "来源id，集中式为集中式id，整租、合租为community_id")
    @TableField("mode_ref_id")
    private Long modeRefId;

    @Schema(description = "小区ID")
    @TableField("community_id")
    private Long communityId;

    @Schema(description = "座栋")
    @TableField("building")
    private String building;

    @Schema(description = "单元")
    @TableField("unit")
    private String unit;

    @Schema(description = "门牌号，分散式独有")
    @TableField("door_number")
    private String doorNumber;

    @Schema(description = "户型")
    @TableField("house_layout_id")
    private Long houseLayoutId;

    @Schema(description = "出租类型：1=整租，2=合租")
    @TableField("rental_type")
    private Integer rentalType;

    @Schema(description = "套内面积")
    @TableField("area")
    private BigDecimal area;

    @Schema(description = "朝向")
    @TableField("direction")
    private String direction;

    @Schema(description = "装修类型：1=豪华装，2=简装，3=精装，4=毛坯，5=清水，6=简约，7=未装修")
    @TableField("decoration_type")
    private Integer decorationType;

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

    @Schema(description = "是否有电梯")
    @TableField("has_elevator")
    private Boolean hasElevator;

    @Schema(description = "是否有燃气")
    @TableField("has_gas")
    private Boolean hasGas;

    @Schema(description = "物业费，每月")
    @TableField("property_fee")
    private BigDecimal propertyFee;

    @Schema(description = "暖气费，每月")
    @TableField("heating_fee")
    private BigDecimal heatingFee;

    @Schema(description = "物业费，每月")
    @TableField("mgmt_fee")
    private BigDecimal mgmtFee;

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

    @Schema(description = "锁定状态：是否锁定")
    @TableField("locked")
    private Boolean locked;

    @Schema(description = "禁用状态：是否已禁用")
    @TableField("closed")
    private Boolean closed;

    @Schema(description = "房源描述、项目介绍")
    @TableField("house_desc")
    private String houseDesc;

    @Schema(description = "商圈介绍、广告语")
    @TableField("business_desc")
    private String businessDesc;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
