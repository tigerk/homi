package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 集中式项目
 * </p>
 *
 * @author tk
 * @since 2025-09-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("focus")
@Schema(name = "Focus", description = "集中式项目")
public class Focus implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "项目编号")
    @TableField("focus_code")
    private String focusCode;

    @Schema(description = "项目名称")
    @TableField("focus_name")
    private String focusName;

    @Schema(description = "项目地址")
    @TableField("communityAddress")
    private String address;

    @Schema(description = "小区ID")
    @TableField("community_id")
    private Long communityId;

    @Schema(description = "门店联系电话")
    @TableField("store_phone")
    private String storePhone;

    @Schema(description = "部门ID")
    @TableField("dept_id")
    private Long deptId;

    @Schema(description = "业务员ID")
    @TableField("salesman_id")
    private Long salesmanId;

    @Schema(description = "设施、从字典dict_data获取并配置")
    @TableField("facilities")
    private String facilities;

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

    @Schema(description = "房间数 为0表示未分配房间")
    @TableField("room_count")
    private Integer roomCount;

    @Schema(description = "房源描述、项目介绍")
    @TableField("house_desc")
    private String houseDesc;

    @Schema(description = "商圈介绍、广告语")
    @TableField("business_desc")
    private String businessDesc;

    @Schema(description = "标签")
    @TableField("tags")
    private String tags;

    @Schema(description = "项目描述")
    @TableField("remark")
    private String remark;

    @Schema(description = "图片列表")
    @TableField("image_list")
    private String imageList;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;
}
