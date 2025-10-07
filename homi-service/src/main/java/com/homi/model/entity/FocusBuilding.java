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
 * 集中楼栋表
 * </p>
 *
 * @author tk
 * @since 2025-09-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("focus_building")
@Schema(name = "FocusBuilding", description = "集中楼栋表")
public class FocusBuilding implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @Schema(description = "公司id")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "集中式ID")
    @TableField("focus_id")
    private Long focusId;

    @Schema(description = "座栋")
    @TableField("building")
    private String building;

    @Schema(description = "单元")
    @TableField("unit")
    private String unit;

    @Schema(description = "每层房源数")
    @TableField("house_count_per_floor")
    private Integer houseCountPerFloor;

    @Schema(description = "房号前缀")
    @TableField("house_prefix")
    private String housePrefix;

    @Schema(description = "房号长度")
    @TableField("number_length")
    private Integer numberLength;

    @Schema(description = "去掉4")
    @TableField("exclude_four")
    private Boolean excludeFour;

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

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

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
}
