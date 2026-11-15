package com.module.setting.application.shared.command;

/**
 * 刪除設定指令物件 (DeleteSettingCommand)
 * <p>
 * 應用層 Command 物件，封裝邏輯刪除設定所需的主鍵 ID。
 * </p>
 *
 * @param id 設定主鍵 ID
 */
public record DeleteSettingCommand(
        Long id
) {
}
