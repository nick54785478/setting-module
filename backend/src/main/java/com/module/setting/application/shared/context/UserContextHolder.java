package com.module.setting.application.shared.context;

/**
 * 使用者上下文持有者 (UserContextHolder)
 * <p>
 * 位於應用層 (Application Layer) 之共用上下文容器，使用 {@link ThreadLocal} 儲存當前執行緒之租戶標識 (Tenant) 與使用者名稱 (Username)，
 * 提供表現層 Filter 寫入、應用層與基礎設施層 (Infrastructure Layer) 存取，無任何外部框架依賴。
 * </p>
 */
public final class UserContextHolder {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();

    private UserContextHolder() {
        // 私有建構子，防止實例化
    }

    /**
     * 設定當前執行緒之租戶標識
     *
     * @param tenant 租戶標識 (例如: "default", "CW")
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
     * 設定當前執行緒之使用者名稱
     *
     * @param username 使用者帳號/名稱 (例如: "nickgh.zhang@cw.com")
     */
    public static void setUsername(String username) {
        CURRENT_USERNAME.set(username);
    }

    /**
     * 取得當前執行緒之使用者名稱
     *
     * @return 當前使用者名稱，若未設定則回傳 null
     */
    public static String getUsername() {
        return CURRENT_USERNAME.get();
    }

    /**
     * 同時設定當前執行緒之租戶標識與使用者名稱
     *
     * @param tenant   租戶標識
     * @param username 使用者名稱
     */
    public static void set(String tenant, String username) {
        CURRENT_TENANT.set(tenant);
        CURRENT_USERNAME.set(username);
    }

    /**
     * 清理當前執行緒之租戶標識與使用者名稱
     * <p>
     * 於請求處理完成後務必呼叫此方法，防止執行緒池複用導致的資料污染與記憶體洩漏。
     * </p>
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USERNAME.remove();
    }
}
