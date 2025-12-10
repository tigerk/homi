package com.homi.model.dao.entity;

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
 * 公司默认数据表
 * </p>
 *
 * @author tk
 * @since 2025-12-01
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("company_init")
@Schema(name = "CompanyInit", description = "公司默认数据表")
public class CompanyInit implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Integer id;

    @Schema(description = "字典默认数据")
    @TableField("dicts")
    private String dicts;

    @Schema(description = "版本号")
    @TableField("ver")
    private Integer ver;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
