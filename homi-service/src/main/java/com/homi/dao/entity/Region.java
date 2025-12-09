package com.homi.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 区域表
 * </p>
 *
 * @author tk
 * @since 2025-07-31
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("region")
@Schema(name = "Region", description = "区域表")
public class Region implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @Schema(description = "父id")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "层级")
    @TableField("deep")
    private Integer deep;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "拼音前缀")
    @TableField("pinyin_prefix")
    private String pinyinPrefix;

    @Schema(description = "拼音")
    @TableField("pinyin")
    private String pinyin;

    @Schema(description = "扩展id")
    @TableField("ext_id")
    private Long extId;

    @Schema(description = "扩展名称")
    @TableField("ext_name")
    private String extName;
}
