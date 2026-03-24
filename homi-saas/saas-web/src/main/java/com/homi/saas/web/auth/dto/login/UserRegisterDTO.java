package com.homi.saas.web.auth.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRegisterDTO {

    /**
     * 公司性质 1：企业 2：个人
     */
    @NotNull(message = "类型不能为空")
    private Integer nature;

    /**
     * 公司名称/个人名称
     */
    @NotBlank(message = "名称不能为空")
    private String companyName;

    /**
     * 公司简称
     */
    private String companyAbbr;

    /**
     * 法定代表人
     */
    @NotBlank(message = "法定代表人不能为空")
    private String legalPerson;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    private String contactName;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "验证码格式不正确")
    private String verificationCode;
}
