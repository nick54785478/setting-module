package com.module.setting.infrastructure.persistence.setting.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 設定展示資料嵌入式值物件 (SettingProfile Value Object)
 * <p>
 * 封裝設定項目之人類可讀名稱 (Name) 與詳細功能描述 (Description)。
 * </p>
 */
@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettingProfile {

    /**
     * 設定名稱 (Setting Name / Display Label)
     * <p>
     * 提供前端 UI 或人類閱讀的業務功能顯示名稱 (例如: "儀錶板", "管理者群組")。
     * </p>
     */
    @Column(name = "name")
    private String name;

    /**
     * 設定描述 (Setting Description)
     * <p>
     * 詳細說明該功能設定之用途、權限意涵或備註 (例如: "Dashboard 相關功能", "有管理者權限的群組")。
     * </p>
     */
    @Column(name = "description")
    private String description;

    /**
     * 工廠方法：建立設定展示資料物件
     *
     * @param name        設定名稱
     * @param description 設定描述
     * @return 建立的 SettingProfile 物件
     */
    public static SettingProfile of(String name, String description) {
        SettingProfile profile = new SettingProfile();
        profile.name = name;
        profile.description = description;
        return profile;
    }
}
