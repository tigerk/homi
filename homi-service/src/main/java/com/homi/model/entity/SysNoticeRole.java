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
 * 通知角色表
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_notice_role")
@Schema(name = "SysNoticeRole", description = "通知角色表")
public class SysNoticeRole implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @TableId("id")
    private Long id;

    @Schema(description = "通知ID，只有在通知公告类型为通知时才存")
    @TableField("notice_id")
    private Long noticeId;

    @Schema(description = "要通知的角色")
    @TableField("role_id")
    private Long roleId;
}
