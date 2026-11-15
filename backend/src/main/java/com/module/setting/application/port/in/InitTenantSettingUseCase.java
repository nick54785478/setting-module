package com.module.setting.application.port.in;

/**
 * 租戶預設設定初始化用例介面 (InitTenantSettingUseCase)
 * <p>
 * 應用層 Inbound Port，定義為指定租戶進行基礎功能設定（ROLE, GROUP, FUNCTION 等）種子資料初始化的業務合約。
 * 具備冪等性 (Idempotent)，多次執行不會產生重複資料或破壞現有已自訂設定。
 * </p>
 */
public interface InitTenantSettingUseCase {

    /**
     * 執行指定租戶的預設功能設定初始化
     *
     * @param tenant 租戶標識 (例如: "default", "tenant-01")
     * @return 實際新增的設定筆數
     */
    int initDefaultSettings(String tenant);
}
