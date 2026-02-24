package com.homi.common.lib.config;

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
     * 涉及到平台、公司管理功能的公共表，不做租户隔离
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/26 09:19
     */
    private final List<String> ignoreTables = Arrays.asList(
        "config",
        "user_role",
        "role_menu",
        "menu",
        // 系统用户表
        "user",
        // 用户公司关联表
        "company_user",
        "company_package",
        "company",
        "region",
        "community",
        "file_meta",
        "room_price_config",
        "room_price_plan",
        "room_detail",
        "lease_room",
        "lease_contract",
        "lease_other_fee",
        "lease_bill_other_fee",
        "tenant_mate",
        "delivery_item",
        // 审批节点表
        "approval_node",
        // 审批动作表
        "approval_action",
        "lease_checkout_fee",
        "sys_notice_role",
        "sys_notice_read"
    );

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

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

        // 分页放置在最后，才能使用租户拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }
}
