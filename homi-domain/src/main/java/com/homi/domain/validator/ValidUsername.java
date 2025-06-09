package com.homi.domain.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {

    /**
     * 校验失败时的默认消息
     */
    String message() default "参数格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
