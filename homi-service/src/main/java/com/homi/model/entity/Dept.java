package com.homi.model.entity;

    import com.baomidou.mybatisplus.annotation.IdType;
    import com.baomidou.mybatisplus.annotation.TableId;
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
    * 部门表
    * </p>
*
* @author tk
* @since 2025-06-12
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    public class Dept implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * 主键
            */
            @TableId(value = "id", type = IdType.AUTO)
    private Long id;

            /**
            * 部门名称
            */
    private String name;

            /**
            * 父节点id
            */
    private Long parentId;

            /**
            * 父节点id路径
            */
    private String treePath;

            /**
            * 显示顺序
            */
    private Integer sort;

            /**
            * 状态（0正常，-1禁用）
            */
    private Integer status;

            /**
            * 创建时间
            */
    private Date createTime;

            /**
            * 创建人
            */
    private Long createBy;

            /**
            * 更新时间
            */
    private Date updateTime;

            /**
            * 更新人
            */
    private Long updateBy;

            /**
            * 是否删除（0否1是）
            */
    private Integer deleted;
}
