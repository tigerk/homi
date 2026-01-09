package com.homi.model.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "公司创建对象")
public class CompanyCreateDTO {
    /**
     * 主键ID
     */
    @Schema(description = "id，修改时需要传")
    private Long id;

    /**
     * 公司名称
     */
    private String name;

    /**
     * 公司简称
     */
    private String abbr;

    /**
     * 区域ID
     */
    private List<Long> regionIds;

    /**
     * 公司LOGO
     */
    private String logo;

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

    @Schema(description = "公司管理员手机号")
    private String adminPhone;

    @Schema(description = "公司管理员密码")
    private String adminPassword;

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
     * 状态（1正常，0禁用）
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
}
