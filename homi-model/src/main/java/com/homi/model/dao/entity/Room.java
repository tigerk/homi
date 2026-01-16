package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 房间表
 * </p>
 *
 * @author tk
 * @since 2025-11-03
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("room")
@Schema(name = "Room", description = "房间表")
public class Room implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @TableField("house_id")
    private Long houseId;

    @Schema(description = "搜索关键字")
    @TableField("keywords")
    private String keywords;

    @Schema(description = "楼层")
    @TableField("floor")
    private Integer floor;

    @TableField("room_number")
    private String roomNumber;

    @Schema(description = "房间类型")
    @TableField("room_type")
    private Integer roomType;

    @Schema(description = "出房价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "面积")
    @TableField("area")
    private BigDecimal area;

    @Schema(description = "朝向")
    @TableField("direction")
    private String direction;

    @Schema(description = "空置开始时间")
    @TableField("vacancy_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date vacancyStartTime;

    @Schema(description = "可出租日期")
    @TableField("available_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date availableDate;

    @TableField("remark")
    private String remark;

    @Schema(description = "房间状态")
    @TableField("room_status")
    private Integer roomStatus;

    @Schema(description = "锁定状态：是否锁定")
    @TableField("locked")
    private Boolean locked;

    @Schema(description = "禁用状态：是否已禁用")
    @TableField("closed")
    private Boolean closed;

    @Schema(description = "出租状态：0 未出租；1 已出租")
    @TableField("leased")
    private Boolean leased;

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

    @Schema(description = "房间特色")
    @TableField("tags")
    private String tags;

    @Schema(description = "设施、从字典dict_data获取并配置")
    @TableField("facilities")
    private String facilities;

    @Schema(description = "图片列表")
    @TableField("image_list")
    private String imageList;

    @Schema(description = "视频")
    @TableField("video_list")
    private String videoList;
}
