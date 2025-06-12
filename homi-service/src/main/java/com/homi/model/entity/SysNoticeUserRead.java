package com.homi.model.entity;

    import com.baomidou.mybatisplus.annotation.IdType;
    import com.baomidou.mybatisplus.annotation.TableId;
    import com.baomidou.mybatisplus.annotation.TableName;
    import java.io.Serializable;
    import java.util.Date;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.ToString;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
* <p>
    * 用户公告通知已读状态表
    * </p>
*
* @author tk
* @since 2025-06-12
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("sys_notice_user_read")
    public class SysNoticeUserRead implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * 主键
            */
            @TableId(value = "id", type = IdType.AUTO)
    private Long id;

            /**
            * 通知公告ID
            */
    private Long noticeId;

            /**
            * 用户ID
            */
    private Long userId;

            /**
            * 是否已读（0否1是）
            */
    private Integer status;

            /**
            * 已读时间
            */
    private Date readTime;

            /**
            * 创建时间
            */
    private Date createTime;

            /**
            * 更新时间
            */
    private Date updateTime;

            /**
            * 是否删除（0否1是）
            */
    private Integer deleted;
}
