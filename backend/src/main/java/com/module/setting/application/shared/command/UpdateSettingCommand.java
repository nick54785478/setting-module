package com.module.setting.application.shared.command;

/**
 * 更新設定指令物件 (UpdateSettingCommand)
 * <p>
 * 應用層 Command 物件，封裝異動現有設定所需的全部業務參數。
 * </p>
 *
 * @param id          設定主鍵 ID
 * @param category    業務功能維度 (可選更動)
 * @param subCategory 業務子分類 (可選更動)
 * @param name        設定顯示名稱
 * @param description 設定詳細描述
 * @param value       設定數值 / 標識值
 * @param priorityNo  順序號 (從 1 開始)
 * @param activeFlag  是否生效 (Y / N)
 */
public record UpdateSettingCommand(
        Long id,
        String category,
        String subCategory,
        String name,
        String description,
        String value,
        Integer priorityNo,
        String activeFlag
) {
}
