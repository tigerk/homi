package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * 房间价格表
 * </p>
 *
 * @author tk
 * @since 2025-10-28
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("room_price_config")
@Schema(name = "RoomPriceConfig", description = "房间价格表")
public class RoomPriceConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "出房价格（单位：元）")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "底价方式：1=固定金额，2=按比例")
    @TableField("floor_price_method")
    private Integer floorPriceMethod;

    @Schema(description = "底价录入值（金额或比例，具体由 low_price_method 决定）")
    @TableField("floor_price_input")
    private BigDecimal floorPriceInput;

    @Schema(description = "计算后的底价金额（冗余列可选）")
    @TableField("floor_price")
    private BigDecimal floorPrice;

    @Schema(description = "其他费用（json）")
    @TableField("other_fees")
    private String otherFees;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

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
