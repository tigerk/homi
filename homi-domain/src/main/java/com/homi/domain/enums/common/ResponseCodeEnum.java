package com.homi.domain.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统各种状态枚举
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:28
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {
    /**
     * 成功
     */
    SUCCESS(0, "请求成功"),

    /**
     * 验证码错误
     */
    VERIFICATION_CODE_ERROR(400, "验证码错误"),

    /**
     * 未登录
     */
    NO_LOGIN(401, "用户未登录"),
    /**
     * 没有操作权限
     */
    AUTHORIZED(403, "无操作权限"),

    /**
     * 404
     */
    NO_FOUND(404, "系统无此资源"),
    /**
     * 系统异常
     */
    SYSTEM_ERROR(500, "系统错误"),
    /**
     * 操作失败
     */
    FAIL(510, "操作失败"),
    /**
     * 参数格式不正确
     */
    VALID_ERROR(520, "参数校验失败"),
    /**
     * 上传失败
     */
    UPLOAD_FAIL(530, "上传失败"),
    /**
     * 创建目录失败
     */
    CREATE_MKR_FAIL(540, "创建目录失败"),
    /**
     * 用户已存在
     */
    USER_EXIST(610, "用户已存在"),
    /**
     * 用户不存在
     */
    USER_NOT_EXIST(620, "用户不存在"),

    /**
     * 账号已被冻结
     */
    USER_FREEZE(630, "账号已被冻结"),

    /**
     * 账号无权限进入后台
     */
    USER_NO_ACCESS(640, "此账号无权限进入后台"),

    /**
     * 角色已被冻结
     */
    ROLE_FREEZE(650, "角色已被禁用"),

    USER_NOT_BIND_COMPANY(650, "账号没有绑定公司"),

    /**
     * 常规登录错误
     */
    LOGIN_ERROR(700, "用户名或密码错误"),
    /**
     * qq登录错误
     */
    QQ_LOGIN_ERROR(710, "qq登录错误"),
    /**
     * 微博登录错误
     */
    WEIBO_LOGIN_ERROR(720, "微博登录错误"),

    /**
     * token异常
     */
    TOKEN_ERROR(9999, "token异常");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String msg;

}
