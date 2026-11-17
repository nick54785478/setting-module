package com.module.setting.application.shared.command;

/**
 * 設定項目排序調整指令 (ReorderSettingItemCommand)
 *
 * @param id         設定主鍵 ID
 * @param priorityNo 欲設定之順序號
 */
public record ReorderSettingItemCommand(
        Long id,
        Integer priorityNo
) {
}
