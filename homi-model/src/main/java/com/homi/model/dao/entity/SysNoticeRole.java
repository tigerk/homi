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
 * 公告角色关联表
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_notice_role")
@Schema(name = "SysNoticeRole", description = "公告角色关联表")
public class SysNoticeRole implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "公告ID")
    @TableField("notice_id")
    private Long noticeId;

    @Schema(description = "角色ID")
    @TableField("role_id")
    private Long roleId;
}
