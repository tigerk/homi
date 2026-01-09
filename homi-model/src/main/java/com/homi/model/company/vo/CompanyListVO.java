package com.homi.model.company.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司名称")
    private String name;

    @Schema(description = "公司简称")
    private String abbr;

    @Schema(description = "公司社会统一信用代码")
    private String uscc;

    @Schema(description = "法人姓名")
    private String legalPerson;

    @Schema(description = "通信地址")
    private String address;

    @Schema(description = "区域ID")
    private Long regionId;

    @Schema(description = "区域ID列表")
    private List<Long> regionIds;

    @Schema(description = "区域名称")
    private Long regionName;

    @Schema(description = "公司LOGO")
    private String logo;

    @Schema(description = "公司网站")
    private String website;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系人手机号")
    private String contactPhone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "公司管理员ID")
    private Long adminUserId;

    @Schema(description = "公司管理员账号")
    private String adminPhone;

    @Schema(description = "账号数量")
    private Integer accountCount;

    @Schema(description = "公司性质 1：企业 2：个人")
    private Integer nature;

    @Schema(description = "公司套餐id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;

    @Schema(description = "公司套餐名称")
    private String packageName;

    @Schema(description = "状态（1正常，0禁用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "更新人")
    private Long updateBy;
}
