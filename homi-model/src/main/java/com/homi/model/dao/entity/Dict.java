package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 字典表
 * </p>
 *
 * @author tk
 * @since 2026-03-03
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("dict")
@Schema(name = "Dict", description = "字典表")
public class Dict implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "公司id")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "父节点")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "字典编码")
    @TableField("dict_code")
    private String dictCode;

    @Schema(description = "字典名称")
    @TableField("dict_name")
    private String dictName;

    @Schema(description = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "状态（0开启 1关闭）")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否隐藏")
    @TableField("hidden")
    private Boolean hidden;

    @Schema(description = "是否来自模板（1是 0否）")
    @TableField("from_template")
    private Boolean fromTemplate;

    @Schema(description = "是否锁定不被模板覆盖（1是 0否）")
    @TableField("locked")
    private Boolean locked;

    @Schema(description = "最后同步模板版本")
    @TableField("template_ver")
    private Integer templateVer;

    @Schema(description = "最后模板同步时间")
    @TableField("sync_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date syncAt;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建者")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新者")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
