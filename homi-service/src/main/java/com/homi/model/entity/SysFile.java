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
 * 文件表
 * </p>
 *
 * @author tk
 * @since 2025-07-23
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_file")
@Schema(name = "SysFile", description = "文件表")
public class SysFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件编号")
    @TableId("id")
    private Long id;

    @Schema(description = "配置编号")
    @TableField("config_id")
    private Long configId;

    @Schema(description = "文件名")
    @TableField("name")
    private String name;

    @Schema(description = "文件路径")
    @TableField("path")
    private String path;

    @Schema(description = "文件 URL")
    @TableField("url")
    private String url;

    @Schema(description = "文件类型")
    @TableField("type")
    private String type;

    @Schema(description = "文件大小")
    @TableField("size")
    private Integer size;

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
