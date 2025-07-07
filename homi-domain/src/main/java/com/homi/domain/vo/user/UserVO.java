package com.homi.domain.vo.user;

import com.homi.domain.vo.dept.DeptSimpleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户返回VO")
public class UserVO {
    /**
     * 主键（用户id）
     */
    private Long id;

    /**
     * 用户名（登录名）
     */
    private String username;

    private Long companyId;

    private Long deptId;

    private DeptSimpleVO dept;

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
    private LocalDateTime birthday;

    /**
     * 状态（0正常，1禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;


    /**
     * 上次登录时间
     */
    private LocalDateTime lastLoginTime;

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
