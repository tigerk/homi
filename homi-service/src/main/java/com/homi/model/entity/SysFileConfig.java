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
    * 文件配置表
    * </p>
*
* @author tk
* @since 2025-06-12
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("sys_file_config")
    public class SysFileConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

            /**
            * 编号
            */
            @TableId(value = "id", type = IdType.AUTO)
    private Long id;

            /**
            * 配置名
            */
    private String name;

            /**
            * 存储器
            */
    private Integer storage;

            /**
            * 备注
            */
    private String remark;

            /**
            * 是否为主配置（0否1是）
            */
    private Integer master;

            /**
            * 存储配置
            */
    private String config;

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
            * 是否删除 0否1是
            */
    private Integer deleted;
}
