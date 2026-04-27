package com.homi.service.bizlog;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.annotation.BizOperateLog;
import com.homi.service.service.owner.BizOperateLogService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BizOperateLogAspect {
    private final BizOperateLogService bizOperateLogService;
    private final ApplicationContext applicationContext;
    private final BizOperateLogOperatorContext operatorContext;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(annotation)")
    public Object around(ProceedingJoinPoint joinPoint, BizOperateLog annotation) throws Throwable {
        Object beforeSnapshot = null;
        if (annotation.saveBeforeSnapshot()) {
            beforeSnapshot = loadSnapshot(annotation.snapshotProvider(), true, joinPoint.getArgs(), null);
        }

        Object result = joinPoint.proceed();

        Object afterSnapshot = null;
        if (annotation.saveAfterSnapshot()) {
            afterSnapshot = loadSnapshot(annotation.snapshotProvider(), false, joinPoint.getArgs(), result);
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
            null,
            signature.getMethod(),
            joinPoint.getArgs(),
            parameterNameDiscoverer
        );
        context.setVariable("result", result);
        context.setVariable("beforeSnapshot", beforeSnapshot);
        context.setVariable("afterSnapshot", afterSnapshot);

        bizOperateLogService.saveLog(
            operatorContext.getCompanyId(),
            annotation.bizType().getCode(),
            evalLong(annotation.bizIdExpr(), context),
            annotation.operateType().getCode(),
            annotation.operateDesc(),
            evalString(annotation.remarkExpr(), context),
            beforeSnapshot,
            afterSnapshot,
            evalJsonMap(annotation.extraDataExpr(), context),
            annotation.sourceType().getCode().isBlank() ? null : annotation.sourceType().getCode(),
            evalLong(annotation.sourceIdExpr(), context),
            operatorContext.getOperatorId(),
            operatorContext.getOperatorName()
        );
        return result;
    }

    private Object loadSnapshot(String providerBeanName, boolean before, Object[] args, Object result) {
        if (CharSequenceUtil.isBlank(providerBeanName)) {
            return null;
        }
        BizOperateLogSnapshotProvider provider = applicationContext.getBean(providerBeanName, BizOperateLogSnapshotProvider.class);
        return before ? provider.getBeforeSnapshot(args) : provider.getAfterSnapshot(args, result);
    }

    private Long evalLong(String expr, MethodBasedEvaluationContext context) {
        if (CharSequenceUtil.isBlank(expr)) {
            return null;
        }
        return parser.parseExpression(expr).getValue(context, Long.class);
    }

    private String evalString(String expr, MethodBasedEvaluationContext context) {
        if (CharSequenceUtil.isBlank(expr)) {
            return null;
        }
        return parser.parseExpression(expr).getValue(context, String.class);
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> evalJsonMap(String expr, MethodBasedEvaluationContext context) {
        if (CharSequenceUtil.isBlank(expr)) {
            return Collections.emptyMap();
        }
        Object value = parser.parseExpression(expr).getValue(context);
        if (value == null) {
            return Collections.emptyMap();
        }
        if (value instanceof java.util.Map<?, ?> map) {
            return (java.util.Map<String, Object>) map;
        }
        return JSONUtil.parseObj(value).toBean(java.util.Map.class);
    }
}
