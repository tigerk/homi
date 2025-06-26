package com.homi.config;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/25
 */

public class MyBatisTenantContext {
    private MyBatisTenantContext() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getCurrentTenant() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
