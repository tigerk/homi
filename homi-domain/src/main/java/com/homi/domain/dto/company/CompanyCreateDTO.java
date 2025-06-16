package com.homi.domain.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "公司创建对象")
public class CompanyCreateDTO {
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

    private String account;

    private String password;

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
}
