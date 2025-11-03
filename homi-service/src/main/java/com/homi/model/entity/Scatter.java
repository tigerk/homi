package com.homi.model.entity;

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
import java.util.Date;

/**
 * <p>
 * 分散式房源扩展表
 * </p>
 *
 * @author tk
 * @since 2025-09-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("scatter")
@Schema(name = "Scatter", description = "分散式房源扩展表")
public class Scatter implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "房源id")
    @TableId("id")
    private Long id;

    @Schema(description = "房源id")
    @TableField("house_id")
    private Long houseId;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "厅")
    @TableField("living_room")
    private Integer livingRoom;

    @Schema(description = "卫")
    @TableField("bathroom")
    private Integer bathroom;

    @Schema(description = "厨")
    @TableField("kitchen")
    private Integer kitchen;

    @Schema(description = "室")
    @TableField("bedroom")
    private Integer bedroom;

    @Schema(description = "楼层")
    @TableField("floor_level")
    private Integer floorLevel;

    @Schema(description = "朝向")
    @TableField("orientation")
    private Integer orientation;

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
