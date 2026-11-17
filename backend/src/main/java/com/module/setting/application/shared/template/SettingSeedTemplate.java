package com.module.setting.application.shared.template;

import com.module.setting.application.shared.command.CreateSettingCommand;
import java.util.List;

/**
 * 預設業務設定種子資料範本 (SettingSeedTemplate)
 * <p>
 * 遵循架構解耦原則，業務設定 (setting 表) 不再預載任何業務假資料。
 * 大維度字典由 SettingCategory 獨立控管（初始化僅保留 SYSTEM 唯一維度），
 * 其餘維度 (ROLE, GROUP, FUNCTION) 與各項業務功能設定均由維運人員手動配置。
 * </p>
 */
public final class SettingSeedTemplate {

    private SettingSeedTemplate() {
    }

    /**
     * 產出全域基礎業務設定範本清單
     *
     * @param tenant 租戶代碼 (建議為指定租戶，如 "CW")
     * @return 業務設定建立指令清單（初始為空清單，不預載任何業務資料）
     */
    public static List<CreateSettingCommand> getDefaultTemplates(String tenant) {
        return List.of();
    }
}
