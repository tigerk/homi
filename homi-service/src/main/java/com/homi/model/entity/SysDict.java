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
    * 字典表
    * </p>
*
* @author tk
* @since 2025-06-12
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("sys_dict")
    public class SysDict implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * 主键id
            */
            @TableId(value = "id", type = IdType.AUTO)
    private Long id;

            /**
            * 字典编码
            */
    private String dictCode;

            /**
            * 字典名称
            */
    private String dictName;

            /**
            * 状态（0开启 1关闭）
            */
    private Integer status;

            /**
            * 是否删除（0否 1是）
            */
    private Integer deleted;

            /**
            * 创建者
            */
    private Long createBy;

            /**
            * 创建时间
            */
    private Date createTime;

            /**
            * 更新者
            */
    private Long updateBy;

            /**
            * 更新时间
            */
    private Date updateTime;

            /**
            * 备注
            */
    private String remark;
}
