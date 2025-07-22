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
 * @since 2025-07-22
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("house.room")
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

    @TableField("vacancy_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date vacancyStartTime;

    @TableField("remark")
    private String remark;

    @TableField("shelf_status")
    private Integer shelfStatus;

    @TableField("room_status")
    private Integer roomStatus;

    @TableField("locked")
    private Boolean locked;

    @Schema(description = "出租状态：0 未出租；1 已出租")
    @TableField("leased")
    private Boolean leased;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("creater_id")
    private Long createrId;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("updater_id")
    private Long updaterId;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @TableField("room_layout_id")
    private Long roomLayoutId;

    @Schema(description = "出房价格")
    @TableField("lease_price")
    private BigDecimal leasePrice;

    @TableField("orientation")
    private Integer orientation;

    @Schema(description = "楼层")
    @TableField("floor_level")
    private Integer floorLevel;
}
