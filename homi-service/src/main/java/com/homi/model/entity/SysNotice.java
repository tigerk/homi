package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 通知公告表
 * </p>
 *
 * @author tk
 * @since 2025-06-12
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("sys_notice")
public class SysNotice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private byte[] content;

    /**
     * 公告类型（1通知 2公告）
     */
    private Integer type;

    /**
     * 公告状态（0正常 1关闭）
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
     * 是否删除 0否1是
     */
    private Integer deleted;

    /**
     * 备注
     */
    private String remark;
}
