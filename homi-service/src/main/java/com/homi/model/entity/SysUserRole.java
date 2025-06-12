package com.homi.model.entity;

    import com.baomidou.mybatisplus.annotation.IdType;
    import com.baomidou.mybatisplus.annotation.TableId;
    import com.baomidou.mybatisplus.annotation.TableName;
    import java.io.Serializable;

    import lombok.Builder;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.ToString;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
* <p>
    * 用户和角色关联表
    * </p>
*
* @author tk
* @since 2025-06-12
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("sys_user_role")
    @Builder
    public class SysUserRole implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * 主键ID
            */
            @TableId(value = "id", type = IdType.AUTO)
    private Long id;

            /**
            * 用户ID
            */
    private Long userId;

            /**
            * 角色ID
            */
    private Long roleId;
}
