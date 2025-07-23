package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 部门和用户关联表
 * </p>
 *
 * @author tk
 * @since 2025-07-23
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("dept_user")
@Schema(name = "DeptUser", description = "部门和用户关联表")
public class DeptUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "部门ID")
    @TableField("dept_id")
    private Long deptId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;
}
