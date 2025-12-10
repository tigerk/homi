package com.homi.saas.web.config;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/13
 */

import com.homi.saas.web.component.LoginUserArgumentResolver;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web 配置类 - 静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    LoginUserArgumentResolver resolver;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件的访问路径
        // 访问路径：/uploads/**
        // 实际路径：uploadPath 配置的路径
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadPath);

        // 保持默认的静态资源映射
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}
