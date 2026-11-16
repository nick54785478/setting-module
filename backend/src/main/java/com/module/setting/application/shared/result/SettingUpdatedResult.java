package com.module.setting.application.shared.result;

/**
 * 設定更新結果資料傳輸物件 (SettingUpdatedResult)
 * <p>
 * 嚴格作為純資料載體 (Pure Data Carrier)，禁止包含任何領域轉換邏輯。
 * </p>
 *
 * @param id          設定主鍵 ID
 * @param tenant      租戶代碼
 * @param category    功能維度 (例如: ROLE, GROUP, FUNCTION)
 * @param subCategory 子分類 (例如: ADMIN, PM, DASHBOARD)
 * @param code        設定代碼 (例如: DASHBOARD_FUNCTION)
 * @param name        設定名稱
 * @param description 設定描述
 * @param value       設定數值 / 標識
 * @param priorityNo  順序號
 * @param activeFlag  有效旗標 (Y / N)
 */
public record SettingUpdatedResult(
        Long id,
        String tenant,
        String category,
        String subCategory,
        String code,
        String name,
        String description,
        String value,
        Integer priorityNo,
        String activeFlag
) {
}
