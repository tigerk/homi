package com.homi.domain.vo.company;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/17
 */

@Data
@Schema(description = "公司列表对象VO")
@AllArgsConstructor
@NoArgsConstructor
public class CompanyListVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;

    private String packageName;

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
