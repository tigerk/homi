package com.homi.common.lib.handler;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理返回
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 11:58
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    public ResponseResult<Void> handlerBusinessException(BizException e) {
        // 打印异常信息
//        log.warn("业务错误信息：" + e.getMessage(), e);
        // 从异常对象中获取提示信息封装返回
        return ResponseResult.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handlerException(Exception e) {
        log.error("系统错误信息：{}", e.getMessage(), e);
        return ResponseResult.error(e.getMessage());
    }

    /**
     * 资源未找到异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseResult<Void> handlerNoResourceFoundException(NoResourceFoundException e) {
        log.warn("系统资源未找到：{}", e.getMessage(), e);
        return ResponseResult.fail(ResponseCodeEnum.NO_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<Void> handlerValidationException(MethodArgumentNotValidException e) {
        log.warn("参数校验失败：{}", e.getMessage(), e);
        String errorMessage = e.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
        return ResponseResult.fail(ResponseCodeEnum.VALID_ERROR.getCode(), errorMessage);
    }

    /**
     * 登录异常处理
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseResult<Void> handlerNotLoginException(NotLoginException nle) {
        log.warn("登录错误信息：{}{}", nle.getMessage(), nle.getType(), nle);
        // 判断场景值，定制化异常信息
        String message;
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未提供Token";
        } else if (nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            // token无效
            message = "登录状态已失效，请重新登录";
        } else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "Token已过期";
        } else if (nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "Token已被顶下线";
        } else if (nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "Token已被踢下线";
        } else {
            message = "当前会话未登录";
        }
        // 返回给前端
        return ResponseResult.fail(ResponseCodeEnum.TOKEN_ERROR.getCode(), message);
    }

    /**
     * 角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseResult<Void> handlerNotRoleException(NotRoleException nre) {
        log.warn("权限校验错误信息：" + nre.getMessage(), nre);
        // 返回给前端
        return ResponseResult.fail(ResponseCodeEnum.AUTHORIZED);
    }

    /**
     * 权限
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseResult<Void> handlerNotPermissionException(NotPermissionException nre) {
        log.warn("无操作权限：" + nre.getMessage(), nre);
        // 返回给前端
        return ResponseResult.fail(ResponseCodeEnum.AUTHORIZED);
    }

    /**
     * 没用，返回不到前端，先放着，前端处理
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseResult<Void> handleFileMaxError(MaxUploadSizeExceededException exception) {
        log.warn("文件过大{},{}", exception.getMessage(), exception);
        return ResponseResult.fail(ResponseCodeEnum.UPLOAD_FAIL.getCode(), "文件过大");
    }

}
