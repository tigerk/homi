package com.homi.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 房间房型表
 * </p>
 *
 * @author tk
 * @since 2025-07-30
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("room_layout")
@Schema(name = "RoomLayout", description = "房间房型表")
public class RoomLayout implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @TableField("house_id")
    private Long houseId;

    @TableField("inside_space")
    private BigDecimal insideSpace;

    @TableField("living_room")
    private Integer livingRoom;

    @TableField("bathroom")
    private Integer bathroom;

    @TableField("kitchen")
    private Integer kitchen;

    @TableField("bedroom")
    private Integer bedroom;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
