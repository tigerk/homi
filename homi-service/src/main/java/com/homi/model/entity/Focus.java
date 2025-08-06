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
 * 集中式房源扩展表
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("house_focus")
@Schema(name = "Focus", description = "集中式房源扩展表")
public class Focus implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @TableField("house_id")
    private Long houseId;

    @TableField("room_prefix")
    private String roomPrefix;

    @Schema(description = "房间号长度")
    @TableField("room_number_length")
    private Integer roomNumberLength;

    @Schema(description = "去掉4")
    @TableField("exclude_four")
    private Boolean excludeFour;

    @Schema(description = "是否已分配")
    @TableField("allocated")
    private Boolean allocated;

    @TableField("floor_total")
    private Integer floorTotal;

    @Schema(description = "关闭的楼层列表json")
    @TableField("closed_floors")
    private String closedFloors;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

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

    @TableField("create_by")
    private Long createBy;
}
