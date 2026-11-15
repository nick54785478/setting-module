package com.module.setting.infrastructure.persistence.setting.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 設定定位範圍嵌入式值物件 (SettingScope Value Object)
 * <p>
 * 作為 Function Configurable 的多維定位坐標 (Multi-dimensional Coordinate)，
 * 完整封裝租戶 (Tenant)、業務功能維度 (Category)、子分類/模組 (SubCategory) 以及唯一識別碼 (Code)。
 * </p>
 */
@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettingScope {

    /**
     * 租戶識別代碼 (Tenant Identifier)
     * <p>
     * 多租戶架構下的資料隔離與識別標籤 (例如: "default", "tenant-01")，不可為空。
     * </p>
     */
    @Column(name = "tenant", nullable = false)
    private String tenant;

    /**
     * 業務功能領域 / 設定維度 (Category / Dimension)
     * <p>
     * 劃分功能設定的大領域維度 (例如: "ROLE", "GROUP", "FUNCTION")，不可為空。
     * </p>
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * 業務子分類 / 模組類型 (Sub-Category / Module)
     * <p>
     * 隸屬於 category 下的第二層分類 (例如: "ADMIN", "PM", "DASHBOARD", "USER")。
     * </p>
     */
    @Column(name = "sub_category")
    private String subCategory;

    /**
     * 設定代碼 (Setting Code / Unique Key)
     * <p>
     * 特定功能或權限項目於租戶內的唯一識別鍵 (例如: "ADMIN_GROUP", "DASHBOARD_FUNCTION")，不可為空。
     * </p>
     */
    @Column(name = "code", nullable = false)
    private String code;

    /**
     * 工廠方法：建立設定多維定位範圍物件
     *
     * @param tenant      租戶標識
     * @param category    業務功能維度 (ROLE, GROUP, FUNCTION 等)
     * @param subCategory 業務子分類 (ADMIN, DASHBOARD 等)
     * @param code        設定唯一識別碼
     * @return 建立的 SettingScope 物件
     */
    public static SettingScope of(String tenant, String category, String subCategory, String code) {
        SettingScope scope = new SettingScope();
        scope.tenant = tenant;
        scope.category = category;
        scope.subCategory = subCategory;
        scope.code = code;
        return scope;
    }
}
