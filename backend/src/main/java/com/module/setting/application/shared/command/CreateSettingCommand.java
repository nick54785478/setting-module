package com.module.setting.application.shared.command;

/**
 * 建立設定指令物件 (CreateSettingCommand)
 * <p>
 * 應用層 Command 物件，封裝新增設定所需的全部業務參數。
 * </p>
 *
 * @param tenant      租戶代碼
 * @param category    業務功能維度 (例如: ROLE, GROUP, FUNCTION)
 * @param subCategory 業務子分類 (例如: ADMIN, PM, DASHBOARD)
 * @param code        設定唯一識別碼 (例如: DASHBOARD_FUNCTION)
 * @param name        設定顯示名稱
 * @param description 設定詳細描述
 * @param value       設定數值 / 標識值
 * @param priorityNo  順序號 (從 1 開始)
 */
public record CreateSettingCommand(
        String tenant,
        String category,
        String subCategory,
        String code,
        String name,
        String description,
        String value,
        Integer priorityNo
) {
}
