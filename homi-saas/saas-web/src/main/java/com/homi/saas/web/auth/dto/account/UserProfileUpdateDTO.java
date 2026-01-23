package com.homi.saas.web.auth.dto.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class UserProfileUpdateDTO {
    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 简介
     */
    @Size(max = 300, message = "简介长度不能超过300个字符")
    private String remark;

    private Integer gender;

    /**
     * 出生日期
     */
    @Past(message = "出生日期必须是过去的时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;
}
