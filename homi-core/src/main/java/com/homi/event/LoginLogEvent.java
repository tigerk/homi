package com.homi.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 登录事件
 */

@Data
public class LoginLogEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录sessionId
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/23 11:32
     */
    private String sessionId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 登录IP地址
     */
    private String ipAddress;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态（0成功 1失败）
     */
    private Integer status;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 登录时间
     */
    private Date loginTime;

}
