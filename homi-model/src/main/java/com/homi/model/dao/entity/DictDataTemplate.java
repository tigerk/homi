package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 字典数据模板表
 * </p>
 *
 * @author tk
 * @since 2026-03-03
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("dict_data_template")
@Schema(name = "DictDataTemplate", description = "字典数据模板表")
public class DictDataTemplate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "归属字典编码")
    @TableField("dict_code")
    private String dictCode;

    @Schema(description = "数据项名称")
    @TableField("name")
    private String name;

    @Schema(description = "数据项值")
    @TableField("value")
    private String value;

    @Schema(description = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "颜色值")
    @TableField("color")
    private String color;

    @Schema(description = "状态（1开启 0关闭）")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否可删除（1可删 0不可删）")
    @TableField("deletable")
    private Boolean deletable;

    @Schema(description = "模板项是否启用")
    @TableField("enabled")
    private Boolean enabled;

    @Schema(description = "模板版本号")
    @TableField("ver")
    private Integer ver;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
