package com.homi.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 系统访问记录
 * </p>
 *
 * @author tk
 * @since 2025-07-30
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("login_log")
@Schema(name = "LoginLog", description = "系统访问记录")
public class LoginLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "访问ID")
    @TableId("id")
    private Long id;

    @Schema(description = "公司id")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "用户账号")
    @TableField("username")
    private String username;

    @TableField("login_token")
    private String loginToken;

    @Schema(description = "登录IP地址")
    @TableField("ip_address")
    private String ipAddress;

    @Schema(description = "登录地点")
    @TableField("login_location")
    private String loginLocation;

    @Schema(description = "浏览器类型")
    @TableField("browser")
    private String browser;

    @Schema(description = "操作系统")
    @TableField("os")
    private String os;

    @Schema(description = "登录状态（0成功 1失败）")
    @TableField("status")
    private Integer status;

    @Schema(description = "提示消息")
    @TableField("message")
    private String message;

    @Schema(description = "登录时间")
    @TableField("login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date loginTime;
}
