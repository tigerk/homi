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
 * 
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("house.house_focus")
@Schema(name = "HouseFocus", description = "")
public class HouseFocus implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @TableField("house_id")
    private Long houseId;

    @TableField("prefix")
    private String prefix;

    @TableField("room_no_length")
    private Integer roomNoLength;

    @Schema(description = "去掉4")
    @TableField("exclude_four")
    private Boolean excludeFour;

    @TableField("allocated")
    private Boolean allocated;

    @TableField("floor_total")
    private Integer floorTotal;

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
