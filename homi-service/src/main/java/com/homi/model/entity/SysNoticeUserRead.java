package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 用户公告通知已读状态表
 * </p>
 *
 * @author tk
 * @since 2025-07-23
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_notice_user_read")
@Schema(name = "SysNoticeUserRead", description = "用户公告通知已读状态表")
public class SysNoticeUserRead implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId("id")
    private BigDecimal id;

    @Schema(description = "通知公告ID")
    @TableField("notice_id")
    private Long noticeId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "是否已读（0否1是）")
    @TableField("status")
    private Integer status;

    @Schema(description = "已读时间")
    @TableField("read_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date readTime;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
