package com.homi.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@EnableTransactionManagement
@Configuration
public class MybatisPlusConfig {

    /**
     * 设计到平台、公司管理功能的公共表，不做租户隔离
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/26 09:19
     */
    private final List<String> ignoreTables = Arrays.asList(
            "user",
            "sys_user_role",
            "sys_role_menu",
            "sys_role",
            "sys_menu",
            "sys_dict_data",
            "sys_dict",
            "sys_config",
            "company_package",
            "company"
    );

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 添加租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            // 返回当前租户ID
            @Override
            public Expression getTenantId() {
                // 从上下文中获取，比如 ThreadLocal、Header、SecurityContext
                Long tenantId = MyBatisTenantContext.getCurrentTenant();
                return new LongValue(tenantId);
            }

            // 设置租户字段名
            @Override
            public String getTenantIdColumn() {
                return "company_id";
            }

            // 指定不需要加入租户条件的表（如字典表等）
            @Override
            public boolean ignoreTable(String tableName) {
                if (Objects.isNull(MyBatisTenantContext.getCurrentTenant())) {
                    return true;
                }

                return ignoreTables.contains(tableName);
            }
        }));

        return interceptor;
    }
}
