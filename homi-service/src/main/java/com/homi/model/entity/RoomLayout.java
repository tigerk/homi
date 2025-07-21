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
 * 
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("house.room_layout")
@Schema(name = "RoomLayout", description = "")
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
}
