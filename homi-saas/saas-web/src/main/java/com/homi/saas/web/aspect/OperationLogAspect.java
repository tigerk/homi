package com.homi.saas.web.aspect;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.event.OperationLogEvent;
import com.homi.common.lib.response.RequestResultEnum;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.utils.JsonUtils;
import com.homi.common.lib.utils.ServletUtils;
import com.homi.common.lib.utils.SpringUtils;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 操作日志记录处理
 *
 * @author Lion Li
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    /**
     * 排除敏感属性字段
     */
    protected static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};


    /**
     * 计时 key
     */
    private static final ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, Log controllerLog) {
        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
            // *========数据库日志=========*//
            OperationLogEvent operationLog = new OperationLogEvent();
            operationLog.setStatus(RequestResultEnum.SUCCESS.getCode());
            // 请求的地址
            String ip = ServletUtils.getClientIP();
            // 获取客户端操作系统
            String os = userAgent.getOperatingSystem().getName();
            operationLog.setOs(os);
            // 获取客户端浏览器
            String browser = userAgent.getBrowser().getName();
            operationLog.setBrowser(browser);

            operationLog.setIpAddress(ip);
            operationLog.setRequestUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            UserLoginVO userLoginVO = (UserLoginVO) StpUtil.getSession().get(SaSession.USER);
            operationLog.setUserId(userLoginVO.getId());
            operationLog.setUsername(userLoginVO.getUsername());
            operationLog.setCompanyId(userLoginVO.getCurCompanyId());

            if (e != null) {
                operationLog.setStatus(RequestResultEnum.FAILURE.getCode());
                operationLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operationLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operationLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operationLog, jsonResult);
            // 设置消耗时间
            StopWatch stopWatch = KEY_CACHE.get();
            stopWatch.stop();
            operationLog.setCostTime(stopWatch.getTime());
            operationLog.setRequestTime(DateUtil.date());
            // 发布事件保存数据库
            SpringUtils.context().publishEvent(operationLog);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("日志异常信息", exp);
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/30 15:55
     *
     * @param joinPoint    参数说明
     * @param log          参数说明
     * @param operationLog 参数说明
     * @param jsonResult   参数说明
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, OperationLogEvent operationLog, Object jsonResult) throws Exception {
        // 设置action动作
        operationLog.setOperationType(log.operationType().getCode());
        // 设置标题
        operationLog.setTitle(log.title());
        // 设置操作人类别
        operationLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operationLog, log.excludeParamNames());
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && ObjectUtil.isNotNull(jsonResult)) {
            operationLog.setJsonResult(StringUtils.substring(JSONUtil.toJsonStr(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operationLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, OperationLogEvent operationLog, String[] excludeParamNames) throws Exception {
        Map<String, String> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        String requestMethod = operationLog.getRequestMethod();
        if (MapUtil.isEmpty(paramsMap)
                && HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            operationLog.setParam(StringUtils.substring(params, 0, 2000));
        } else {
            MapUtil.removeAny(paramsMap, EXCLUDE_PROPERTIES);
            MapUtil.removeAny(paramsMap, excludeParamNames);
            operationLog.setParam(StringUtils.substring(JSONUtil.toJsonStr(paramsMap), 0, 2000));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        StringJoiner params = new StringJoiner(" ");
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                String str = JSONUtil.toJsonStr(o);
                if (!(o instanceof List<?>)) {
                    Dict dict = JSONUtil.toBean(str, Dict.class);
                    if (MapUtil.isNotEmpty(dict)) {
                        MapUtil.removeAny(dict, EXCLUDE_PROPERTIES);
                        MapUtil.removeAny(dict, excludeParamNames);
                        str = JSONUtil.toJsonStr(dict);
                    }
                }

                params.add(str);
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                if (value instanceof MultipartFile) {
                    return true;
                }
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                if (value instanceof MultipartFile) {
                    return true;
                }
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
