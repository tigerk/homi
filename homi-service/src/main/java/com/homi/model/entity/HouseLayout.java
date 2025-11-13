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
 * 房型设置
 * </p>
 *
 * @author tk
 * @since 2025-09-19
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

    @Schema(description = "房源租赁类型：1=集中式，2=分散式")
    @TableField("lease_mode")
    private Integer leaseMode;

    @Schema(description = "来源id")
    @TableField("mode_ref_id")
    private Long modeRefId;

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

    @Schema(description = "标签")
    @TableField("tags")
    private String tags;

    @Schema(description = "设施、从字典dict_data获取并配置")
    @TableField("facilities")
    private String facilities;

    @Schema(description = "图片列表")
    @TableField("image_list")
    private String imageList;

    @Schema(description = "视频列表")
    @TableField("video_list")
    private String videoList;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
