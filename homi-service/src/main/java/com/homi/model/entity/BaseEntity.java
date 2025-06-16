package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 公司表
 * </p>
 *
 * @author tk
 * @since 2025-06-16
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 公司名称
     */
    private String name;

    /**
     * 城市编码
     */
    private String cityCode;

    /**
     * 公司LOGO
     */
    private String logo;

    /**
     * 公司简称
     */
    private String abbr;

    /**
     * 公司网站
     */
    private String website;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系人手机号
     */
    private String contactPhone;

    /**
     * 邮箱号
     */
    private String email;

    /**
     * 账号数量
     */
    private Integer accountCount;

    /**
     * 法人姓名
     */
    private String legalPerson;

    /**
     * 公司社会统一信用代码
     */
    private String uscc;

    /**
     * 通信地址
     */
    private String address;

    /**
     * 公司性质 1：企业 2：个人
     */
    private Integer nature;

    /**
     * 公司套餐id
     */
    private Long packageId;

    /**
     * 状态（0正常，-1禁用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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
    @TableLogic
    private Integer deleted;
}
