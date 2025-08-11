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
 * @since 2025-08-11
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("focus")
@Schema(name = "Focus", description = "集中式房源扩展表")
public class Focus implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @Schema(description = "公司id")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "房源id")
    @TableField("house_id")
    private Long houseId;

    @Schema(description = "房间号前缀")
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

    @Schema(description = "总楼层")
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
