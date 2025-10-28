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
 * 房间租金方案表
 * </p>
 *
 * @author tk
 * @since 2025-10-28
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("room_price_plan")
@Schema(name = "RoomPricePlan", description = "房间租金方案表")
public class RoomPricePlan implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "租金方案名称")
    @TableField("plan_name")
    private String planName;

    @Schema(description = "租金方案类型（如：长期/短租/节假日）")
    @TableField("plan_type")
    private String planType;

    @Schema(description = "出房价格比例（百分比，如 12.50 表示 12.5%）")
    @TableField("price_ratio")
    private BigDecimal priceRatio;

    @Schema(description = "出房价格（若为固定价格）")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "其他费用")
    @TableField("other_fees")
    private String otherFees;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_by")
    private Long updateBy;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
