package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 住宅小区表
 * </p>
 *
 * @author tk
 * @since 2025-09-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("community")
@Schema(name = "Community", description = "住宅小区表")
public class Community implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "小区名称")
    @TableField("name")
    private String name;

    @Schema(description = "小区别名/常用名")
    @TableField("alias")
    private String alias;

    @Schema(description = "省份")
    @TableField("province")
    private String province;

    @Schema(description = "城市ID，对应的regionId")
    @TableField("city_id")
    private Long cityId;

    @Schema(description = "城市")
    @TableField("city")
    private String city;

    @Schema(description = "区/县")
    @TableField("district")
    private String district;

    @Schema(description = "街道/乡镇")
    @TableField("township")
    private String township;

    @Schema(description = "行政区划代码")
    @TableField("adcode")
    private String adcode;

    @Schema(description = "详细地址")
    @TableField("address")
    private String address;

    @Schema(description = "商圈")
    @TableField("business_area")
    private String businessArea;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "建成年份")
    @TableField("built_year")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date builtYear;

    @Schema(description = "楼栋数")
    @TableField("building_count")
    private Integer buildingCount;

    @Schema(description = "户数")
    @TableField("household_count")
    private Integer householdCount;

    @Schema(description = "绿化率(%)")
    @TableField("greening_rate")
    private BigDecimal greeningRate;

    @Schema(description = "容积率")
    @TableField("plot_ratio")
    private BigDecimal plotRatio;

    @Schema(description = "物业公司")
    @TableField("property_company")
    private String propertyCompany;

    @Schema(description = "开发商")
    @TableField("developer")
    private String developer;

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
