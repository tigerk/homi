package com.homi.dao.entity;

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
 * 参数配置表
 * </p>
 *
 * @author tk
 * @since 2025-07-30
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("config")
@Schema(name = "Config", description = "参数配置表")
public class Config implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "参数主键")
    @TableId("id")
    private Integer id;

    @Schema(description = "参数名称")
    @TableField("config_name")
    private String configName;

    @Schema(description = "参数键名")
    @TableField("config_key")
    private String configKey;

    @Schema(description = "参数键值")
    @TableField("config_value")
    private String configValue;

    @Schema(description = "创建者")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新者")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否系统内置（0否1是）")
    @TableField("type")
    private Integer type;
}
