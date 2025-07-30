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
 * 房间表
 * </p>
 *
 * @author tk
 * @since 2025-07-30
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

    @TableField("product_type")
    private Integer productType;

    @TableField("inside_space")
    private BigDecimal insideSpace;

    @TableField("room_number")
    private String roomNumber;

    @Schema(description = "出房价格")
    @TableField("lease_price")
    private BigDecimal leasePrice;

    @TableField("vacancy_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date vacancyStartTime;

    @Schema(description = "朝向")
    @TableField("orientation")
    private Integer orientation;

    @Schema(description = "楼层")
    @TableField("floor_level")
    private Integer floorLevel;

    @TableField("remark")
    private String remark;

    @Schema(description = "房间房型id")
    @TableField("room_layout_id")
    private Long roomLayoutId;

    @TableField("shelf_status")
    private Integer shelfStatus;

    @TableField("room_status")
    private Integer roomStatus;

    @TableField("locked")
    private Boolean locked;

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
