package com.homi.common.lib.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统各种状态枚举，0-500 是保留状态码，500 以上是自定义状态码
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
     * 账号、登录错误码
     */
    PASSWORD_CANNOT_BE_EMPTY(600, "密码不能为空"),
    USER_EXIST(601, "用户已存在"),
    USER_NO_MENU_ACCESS(602, "此账号无任何菜单权限，无法进入后台"),
    USER_NOT_EXIST(603, "用户不存在"),
    USER_FREEZE(604, "账号已被冻结"),
    USER_NO_ACCESS(605, "此账号无权限进入后台"),
    ROLE_FREEZE(606, "角色已被禁用"),
    USER_NOT_BIND_COMPANY(607, "当前账号未绑定公司"),
    LOGIN_ERROR(608, "用户名或密码错误"),
    ADMIN_EXIST(609, "账号已存在，请使用该账号登录即可。"),
    QQ_LOGIN_ERROR(610, "qq登录错误"),
    WEIBO_LOGIN_ERROR(611, "微博登录错误"),
    WECHAT_NOT_BIND(612, "微信账号未绑定，请先绑定"),
    WECHAT_BIND_CONFLICT(613, "该微信已绑定其他账号"),
    WECHAT_LOGIN_ERROR(614, "微信登录错误"),
    /**
     * 合同错误码
     */
    PDF_GENERATE_ERROR(701, "生成PDF失败"),
    CONTRACT_TEMPLATE_ERROR(702, "合同模板错误"),

    /**
     * 系统错统一使用 900 开头
     */
    DICT_NOT_FOUND(801, "字典不存在"),
    PARAM_ERROR(802, "参数错误"),
    DATA_NOT_FOUND(803, "数据不存在"),
    OPERATION_FAILED(804, "操作失败"),

    /**
     * 支付记录错误吗
     */
    PAYMENT_FLOW_ALREADY_EXISTS(901, "支付记录已存在"),

    /**
     * token异常
     */
    TOKEN_ERROR(9999, "token异常"),

    ;

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String msg;

}
