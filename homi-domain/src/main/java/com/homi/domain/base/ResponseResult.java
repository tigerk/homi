package com.homi.domain.base;

import com.homi.domain.enums.common.ResponseCodeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回数据格式封装
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 01:20
 *
 * @param <T> 数据类型
 */
@Data
public final class ResponseResult<T> implements Serializable {

    /**
     * 响应码
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    private ResponseResult() {
        this.code = ResponseCodeEnum.SUCCESS.getCode();
        this.message = ResponseCodeEnum.SUCCESS.getMsg();
    }

    private ResponseResult(ResponseCodeEnum responseCodeEnum) {
        this.code = responseCodeEnum.getCode();
        this.message = responseCodeEnum.getMsg();
    }

    private ResponseResult(String message) {
        this.code = ResponseCodeEnum.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    private ResponseResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private ResponseResult(T data) {
        this();
        this.data = data;
    }

    private ResponseResult(String message, T data) {
        this.code = ResponseCodeEnum.SUCCESS.getCode();
        this.message = message;
        this.data = data;
    }

    /**
     * 业务处理成功,无数据返回
     */
    public static ResponseResult<Void> ok() {
        return new ResponseResult<>();
    }

    /**
     * 业务处理成功，有数据返回
     */
    public static <T> ResponseResult<T> ok(T data) {
        return new ResponseResult<>(data);
    }

    /**
     * 业务处理成功，有数据以及成功的具体信息返回
     */

    public static <T> ResponseResult<T> ok(String message, T data) {
        return new ResponseResult<>(message, data);
    }

    /**
     * 业务处理失败
     */
    public static ResponseResult<Void> fail(ResponseCodeEnum errorCode) {
        return new ResponseResult<>(errorCode);
    }

    /**
     * 业务处理失败，返回失败码以及失败信息
     */
    public static ResponseResult<Void> fail(Integer code, String message) {
        return new ResponseResult<>(code, message);
    }

    /**
     * 系统错误
     */
    public static ResponseResult<Void> error() {
        return new ResponseResult<>(ResponseCodeEnum.SYSTEM_ERROR);
    }


    /**
     * 系统详细错误
     */
    public static ResponseResult<Void> error(String message) {
        return new ResponseResult<>(message);
    }
}
