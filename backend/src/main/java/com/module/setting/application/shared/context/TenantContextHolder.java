package com.module.setting.application.shared.context;

/**
 * 租戶上下文持有者 (TenantContextHolder)
 * <p>
 * 位於應用層 (Application Layer) 之共用上下文容器，使用 {@link ThreadLocal} 儲存當前執行緒之租戶標識 (Tenant)，
 * 提供表現層 Filter 寫入、應用層與基礎設施層 (Infrastructure Layer) 存取，無任何外部框架依賴。
 * </p>
 */
public final class TenantContextHolder {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContextHolder() {
        // 私有建構子，防止實例化
    }

    /**
     * 設定當前執行緒之租戶標識
     *
     * @param tenant 租戶標識 (例如: "default", "tenant-01")
     */
    public static void setTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    /**
     * 取得當前執行緒之租戶標識
     *
     * @return 當前租戶標識，若未設定則回傳 null
     */
    public static String getTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * 清理當前執行緒之租戶標識
     * <p>
     * 於請求處理完成後務必呼叫此方法，防止執行緒池複用導致的資料污染與記憶體洩漏。
     * </p>
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
