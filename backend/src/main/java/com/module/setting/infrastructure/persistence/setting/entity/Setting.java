package com.module.setting.infrastructure.persistence.setting.entity;

import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.setting.vo.SettingProfile;
import com.module.setting.infrastructure.persistence.setting.vo.SettingScope;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 功能設定持久化實體 (Setting Entity)
 * <p>
 * 作為系統之「Function Configurable」通用設定實體，彈性定義系統各模組參數、角色、群組及功能節點。
 * 採用方案 A 結構，透過 SettingScope 封裝多維定位坐標，SettingProfile 封裝展示屬性。
 * </p>
 */
@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "setting", uniqueConstraints = {
        @UniqueConstraint(name = "uk_setting_scope", columnNames = {"tenant", "category", "code"})
}, indexes = {
        @Index(name = "idx_tenant_category_priority", columnList = "tenant, category, priority_no"),
        @Index(name = "idx_tenant_active_priority", columnList = "tenant, active_flag, priority_no"),
        @Index(name = "idx_priority_no", columnList = "priority_no")
})
@EntityListeners(AuditingEntityListener.class)
public class Setting {

    /**
     * 設定主鍵識別碼 (Primary Key ID)
     * <p>
     * 由資料庫自動遞增生成的唯一主鍵 ID。
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 設定多維定位範圍 (Setting Scope)
     * <p>
     * 封裝租戶 (tenant)、功能維度 (category)、業務子分類 (subCategory) 與設定代碼 (code)。
     * </p>
     */
    @Embedded
    private SettingScope scope;

    /**
     * 設定展示與描述資料 (Setting Profile)
     * <p>
     * 封裝功能顯示名稱 (name) 與詳細說明 (description)。
     * </p>
     */
    @Embedded
    private SettingProfile profile;

    /**
     * 設定數值 (Setting Value)
     * <p>
     * 儲存功能配置值、路由標識或對應內容 (例如: "Dashboard", "User", "管理者")，
     * 亦支援儲存大容量 JSON 配置或動態頁面 Schema / Data，因此定義為 TEXT。
     * </p>
     */
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    /**
     * 順序號 / 排序優先級 (Priority Number)
     * <p>
     * 決定選單列表、功能配置清單的顯示先後排序，從 1 開始遞增。
     * </p>
     */
    @Column(name = "priority_no", nullable = false)
    private Integer priorityNo = 1;

    /**
     * 是否有效旗標 (Active Flag)
     * <p>
     * 標記該設定是否處於有效狀態，採用枚舉 YesNo（Y: 有效，N: 邏輯刪除/停用），預設為 Y。
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "active_flag")
    private YesNo activeFlag = YesNo.Y;

    /**
     * 工廠方法：建立一筆新設定
     *
     * @param scope      設定多維定位坐標
     * @param profile    設定展示名稱與描述
     * @param value      設定值
     * @param priorityNo 順序號
     * @return 建立的 Setting 實體
     */
    public static Setting create(SettingScope scope, SettingProfile profile, String value, Integer priorityNo) {
        Setting setting = new Setting();
        setting.scope = scope;
        setting.profile = profile;
        setting.value = value;
        setting.priorityNo = priorityNo != null ? priorityNo : 1;
        setting.activeFlag = YesNo.Y;
        return setting;
    }

    /**
     * 修改設定內容 (保留原有 Scope 坐標)
     *
     * @param profile    設定展示名稱與描述
     * @param value      設定值
     * @param priorityNo 順序號
     * @param activeFlag 是否生效 (Y / N)
     */
    public void update(SettingProfile profile, String value, Integer priorityNo, String activeFlag) {
        this.profile = profile;
        this.value = value;
        this.priorityNo = priorityNo;
        this.activeFlag = YesNo.valueOf(activeFlag);
    }

    /**
     * 修改完整設定內容 (含 Scope 坐標重定)
     *
     * @param scope      設定多維定位坐標
     * @param profile    設定展示名稱與描述
     * @param value      設定值
     * @param priorityNo 順序號
     * @param activeFlag 是否生效 (Y / N)
     */
    public void update(SettingScope scope, SettingProfile profile, String value, Integer priorityNo, String activeFlag) {
        if (scope != null) {
            this.scope = scope;
        }
        this.profile = profile;
        this.value = value;
        this.priorityNo = priorityNo;
        this.activeFlag = YesNo.valueOf(activeFlag);
    }

    /**
     * 更新排序順序號
     *
     * @param priorityNo 順序號
     */
    public void updatePriority(Integer priorityNo) {
        if (priorityNo != null && priorityNo > 0) {
            this.priorityNo = priorityNo;
        }
    }

    /**
     * 邏輯刪除 (標記 activeFlag = 'N')
     */
    public void delete() {
        this.activeFlag = YesNo.N;
    }
}
