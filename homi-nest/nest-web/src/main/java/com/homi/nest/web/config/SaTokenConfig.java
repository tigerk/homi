package com.homi.nest.web.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截后台请求
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/17 12:16
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SaTokenConfig implements WebMvcConfigurer {
    private static final String ADMIN_PREFIX = "/nest";


    /**
     * 注册 Sa-Token 的拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义认证规则
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 后台登录校验以及角色校验
            SaRouter.notMatch(ADMIN_PREFIX.concat("/login"),
                    ADMIN_PREFIX.concat("/register"),
                    ADMIN_PREFIX.concat("/test/**"),
                    ADMIN_PREFIX.concat("/token/refresh"),
                    ADMIN_PREFIX.concat("/sysFile/check-file/**")
            ).check(r -> {
                StpUtil.checkLogin();
                // 续签
                StpUtil.renewTimeout(SaManager.getConfig().getTimeout());
            });
        })).addPathPatterns("/**");
        // 重复提交
    }

    /**
     * Sa-Token 整合 jwt (Simple 简单模式)
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * 解决跨域问题
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    SaHolder.getResponse()
                            // ---------- 设置跨域响应头 ----------
                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "*")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600");
                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match(SaHttpMethod.OPTIONS).free(r -> log.info("--------OPTIONS预检请求，不做处理")).back();
                });
    }

}
