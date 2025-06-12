package com.homi.model.entity;

    import com.baomidou.mybatisplus.annotation.IdType;
    import com.baomidou.mybatisplus.annotation.TableId;
    import com.baomidou.mybatisplus.annotation.TableName;
    import java.io.Serializable;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.ToString;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
* <p>
    * 通知角色表
    * </p>
*
* @author tk
* @since 2025-06-12
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("sys_notice_role")
    public class SysNoticeRole implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * ID
            */
            @TableId(value = "id", type = IdType.AUTO)
    private Long id;

            /**
            * 通知ID，只有在通知公告类型为通知时才存
            */
    private Long noticeId;

            /**
            * 要通知的角色
            */
    private Long roleId;
}
