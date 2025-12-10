package com.homi.model.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "用户返回VO")
public class PlatformUserVO {
    /**
     * 主键（用户id）
     */
    private Long id;

    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 邮箱号
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 简介
     */
    private String remark;

    /**
     * 性别（0未知，1男，2女）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date birthday;

    @Schema(description = "用户类型，参考UserTypeEnum")
    private Integer userType;

    @Schema(description = "是否可修改")
    private Boolean canUpdate;

    /**
     * 状态（0正常，1禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 创建人
     */
    private String createBy;


    /**
     * 上次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 最后登录IP地址
     */
    private String ipAddress;

    /**
     * 最后登录IP来源
     */
    private String ipSource;

    /**
     * 注册来源
     */
    private String registerSource;

}
