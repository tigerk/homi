package com.homi.common.lib.annotation;

import com.homi.common.lib.enums.biz.BizOperateBizTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.common.lib.enums.biz.BizOperateTypeEnum;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizOperateLog {

    BizOperateBizTypeEnum bizType();

    BizOperateTypeEnum operateType();

    String operateDesc();

    String bizIdExpr() default "";

    String remarkExpr() default "";

    String extraDataExpr() default "";

    BizOperateSourceTypeEnum sourceType() default BizOperateSourceTypeEnum.NONE;

    String sourceIdExpr() default "";

    boolean saveBeforeSnapshot() default false;

    boolean saveAfterSnapshot() default false;

    String snapshotProvider() default "";
}
