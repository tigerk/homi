package com.homi.config;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/25
 */

public class MyBatisTenantContext {
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    private MyBatisTenantContext() {
        throw new IllegalStateException("Utility class");
    }

    public static Long getCurrentTenant() {
        return TENANT_ID.get();
    }

    public static void setCurrentTenant(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
