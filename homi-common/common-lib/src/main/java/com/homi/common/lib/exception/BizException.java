package com.homi.common.lib.exception;

import com.homi.common.lib.response.ResponseCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BizException extends RuntimeException {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 描述
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public BizException() {

    }

    public BizException(ResponseCodeEnum statusCodeEnum) {
        this.code = statusCodeEnum.getCode();
        this.message = statusCodeEnum.getMsg();
    }

    public BizException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BizException(String message) {
        this.code = ResponseCodeEnum.FAIL.getCode();
        this.message = message;
    }
}
