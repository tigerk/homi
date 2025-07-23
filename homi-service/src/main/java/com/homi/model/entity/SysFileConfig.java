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
 * 文件配置表
 * </p>
 *
 * @author tk
 * @since 2025-07-23
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_file_config")
@Schema(name = "SysFileConfig", description = "文件配置表")
public class SysFileConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId("id")
    private Long id;

    @Schema(description = "配置名")
    @TableField("name")
    private String name;

    @Schema(description = "存储器")
    @TableField("storage")
    private Integer storage;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否为主配置（0否1是）")
    @TableField("master")
    private Integer master;

    @Schema(description = "存储配置")
    @TableField("config")
    private String config;

    @Schema(description = "创建者")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新者")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
