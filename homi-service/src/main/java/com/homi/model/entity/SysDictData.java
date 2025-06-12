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
    * 字典数据表
    * </p>
*
* @author tk
* @since 2025-06-12
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("sys_dict_data")
    public class SysDictData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * 主键ID
            */
            @TableId(value = "id", type = IdType.AUTO)
    private Long id;

            /**
            * 字典ID
            */
    private Long dictId;

            /**
            * 数据项名称
            */
    private String name;

            /**
            * 数据项值
            */
    private String value;

            /**
            * 排序
            */
    private Integer sortOrder;

            /**
            * 颜色值
            */
    private String color;

            /**
            * 状态（0开启 1关闭）
            */
    private Integer status;

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
