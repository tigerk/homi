package com.homi.model.dao.entity;

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
 * 站内信/个人消息表
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_message")
@Schema(name = "SysMessage", description = "站内信/个人消息表")
public class SysMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    @TableId("id")
    private Long id;

    @Schema(description = "公司/租户ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "发送人（0=系统自动发送）")
    @TableField("sender_id")
    private Long senderId;

    @Schema(description = "接收人")
    @TableField("receiver_id")
    private Long receiverId;

    @Schema(description = "消息标题")
    @TableField("title")
    private String title;

    @Schema(description = "消息内容")
    @TableField("content")
    private String content;

    @Schema(description = "1=系统消息 2=租约提醒 3=缴费提醒 4=报修通知 5=私信")
    @TableField("msg_type")
    private Integer msgType;

    @Schema(description = "关联业务类型（contract/bill/repair/room 等）")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "关联业务ID，前端据此跳转到对应详情页")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "0=未读 1=已读")
    @TableField("is_read")
    private Boolean isRead;

    @Schema(description = "阅读时间")
    @TableField("read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date readTime;

    @Schema(description = "发送方删除：0=否 1=是")
    @TableField("deleted_by_sender")
    private Boolean deletedBySender;

    @Schema(description = "接收方删除：0=否 1=是")
    @TableField("deleted_by_receiver")
    private Boolean deletedByReceiver;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
