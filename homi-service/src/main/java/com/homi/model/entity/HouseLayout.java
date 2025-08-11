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
 * 房型设置
 * </p>
 *
 * @author tk
 * @since 2025-08-11
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("house_layout")
@Schema(name = "HouseLayout", description = "房型设置")
public class HouseLayout implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId("id")
    private Long id;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "房源id")
    @TableField("house_id")
    private Long houseId;

    @Schema(description = "房型名称")
    @TableField("layout_name")
    private String layoutName;

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

    @Schema(description = "面积")
    @TableField("area")
    private BigDecimal area;

    @Schema(description = "朝向")
    @TableField("direction")
    private Integer direction;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人")
    @TableField("creater_id")
    private Long createrId;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人")
    @TableField("updater_id")
    private Long updaterId;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
