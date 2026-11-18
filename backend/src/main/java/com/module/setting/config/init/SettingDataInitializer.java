package com.module.setting.config.init;

import com.module.setting.application.port.in.InitTenantSettingUseCase;
import com.module.setting.infrastructure.persistence.category.entity.SettingCategory;
import com.module.setting.infrastructure.persistence.category.repository.SettingCategoryJpaRepository;
import com.module.setting.infrastructure.persistence.setting.entity.Setting;
import com.module.setting.infrastructure.persistence.setting.repository.SettingJpaRepository;
import com.module.setting.infrastructure.persistence.setting.vo.SettingProfile;
import com.module.setting.infrastructure.persistence.setting.vo.SettingScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系統預設設定資料自動初始化執行器 (SettingDataInitializer)
 * <p>
 * 集中於專案根目錄下的 config 套件，於 Spring Boot 啟動完成後：
 * 1. 於 setting_category 表初始化 SYSTEM 與 LOCALE 基礎維度字典。
 * 2. 針對預設租戶 (CW) 自動注入標準基準語系 (zh-TW, en-US, ja-JP)。
 * 其餘業務設定均由維運人員手動配置。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "setting.init.enabled", havingValue = "true", matchIfMissing = true)
public class SettingDataInitializer implements ApplicationRunner {

    private final InitTenantSettingUseCase initTenantSettingUseCase;
    private final SettingCategoryJpaRepository categoryJpaRepository;
    private final SettingJpaRepository settingJpaRepository;

    @Value("${setting.init.default-tenant:CW}")
    private String defaultTenant;

    public SettingDataInitializer(
            InitTenantSettingUseCase initTenantSettingUseCase,
            SettingCategoryJpaRepository categoryJpaRepository,
            SettingJpaRepository settingJpaRepository) {
        this.initTenantSettingUseCase = initTenantSettingUseCase;
        this.categoryJpaRepository = categoryJpaRepository;
        this.settingJpaRepository = settingJpaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("[SettingModule] Starting automated seed data initialization for default tenant: '{}'...", defaultTenant);
        try {
            // 1. 初始化 Meta Category 維度字典：SYSTEM 基礎維度、LOCALE 語系維度與 MENU 選單維度
            initCategories();

            // 2. 初始化基準語系配置 (zh-TW, en-US, ja-JP)
            initBaselineLocales();

            // 3. 初始化側邊欄導航選單配置 (核心業務配置、系統與安全維運)
            initBaselineMenus();

            // 4. 初始化基準業務功能設定
            int initializedCount = initTenantSettingUseCase.initDefaultSettings(defaultTenant);
            log.info("[SettingModule] Seed data initialization completed. Added {} new setting(s).", initializedCount);
        } catch (Exception e) {
            log.error("[SettingModule] Failed to initialize default settings for tenant '{}'", defaultTenant, e);
        }
    }

    private void initCategories() {
        var categories = List.of(
                SettingCategory.of(defaultTenant, "SYSTEM", "系統配置", "系統底層與全域基礎設施配置維度", 1),
                SettingCategory.of(defaultTenant, "LOCALE", "系統語系配置", "管理各租戶支援的系統語言環境與國際化設定", 2),
                SettingCategory.of(defaultTenant, "MENU", "側邊欄選單配置", "管理側邊導航列之分組、項目、圖示、路由與顯示順序", 3),
                SettingCategory.of(defaultTenant, "DYNAMIC_PAGE", "動態頁面配置", "管理各租戶專屬自訂動態頁面與表單填寫資料", 4)
        );

        for (var cat : categories) {
            if (!categoryJpaRepository.existsByTenantAndCode(cat.getTenant(), cat.getCode())) {
                categoryJpaRepository.save(cat);
                log.info("[SettingModule] Initialized Meta Category: {} ({}) for tenant '{}'",
                        cat.getName(), cat.getCode(), cat.getTenant());
            }
        }
    }

    private void initBaselineLocales() {
        // 各 Tenant 獨立維護語系，此處僅為預設展示租戶 (CW) 提供開箱即用之基準語系，不設置全域 ALL 語系
        List<String> targetTenants = List.of(defaultTenant);

        for (String tenant : targetTenants) {
            List<Setting> locales = List.of(
                    Setting.create(
                            SettingScope.of(tenant, "LOCALE", "UI", "zh-TW"),
                            SettingProfile.of("繁體中文", "繁體中文 (台灣)"),
                            "DEFAULT",
                            1
                    ),
                    Setting.create(
                            SettingScope.of(tenant, "LOCALE", "UI", "en-US"),
                            SettingProfile.of("English", "English (United States)"),
                            "",
                            2
                    ),
                    Setting.create(
                            SettingScope.of(tenant, "LOCALE", "UI", "ja-JP"),
                            SettingProfile.of("日本語", "日本語 (日本)"),
                            "",
                            3
                    )
            );

            for (Setting s : locales) {
                if (!settingJpaRepository.existsByScopeTenantAndScopeCategoryAndScopeCode(
                        s.getScope().getTenant(), s.getScope().getCategory(), s.getScope().getCode())) {
                    settingJpaRepository.save(s);
                    log.info("[SettingModule] Initialized Locale: {} ({}) for tenant '{}'",
                            s.getProfile().getName(), s.getScope().getCode(), s.getScope().getTenant());
                }
            }
        }
    }

    private void initBaselineMenus() {
        List<Setting> menus = List.of(
                // 核心業務配置群組
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "核心業務配置", "MENU_SETTING"),
                        SettingProfile.of("功能設定管理", "核心業務設定項目增刪改查管理"),
                        "{\"icon\":\"pi pi-sliders-h\",\"badge\":\"核心\",\"view\":\"setting\"}",
                        1
                ),
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "核心業務配置", "MENU_CATEGORY"),
                        SettingProfile.of("元資料字典", "維度大分類字典維護"),
                        "{\"icon\":\"pi pi-database\",\"badge\":\"元資料\",\"view\":\"category\"}",
                        2
                ),
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "核心業務配置", "MENU_TENANT"),
                        SettingProfile.of("租戶隔離中心", "多租戶獨立數據空間與隔離策略配置"),
                        "{\"icon\":\"pi pi-building\",\"view\":\"dynamic-page\"}",
                        3
                ),
                // 系統與安全維運群組
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "系統與安全維運", "MENU_ROLE"),
                        SettingProfile.of("系統權限角色", "RBAC 角色與細粒度操作授權矩陣"),
                        "{\"icon\":\"pi pi-shield\"}",
                        1
                ),
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "系統與安全維運", "MENU_AUDIT"),
                        SettingProfile.of("操作稽核日誌", "使用者操作變更歷史與安全稽核軌跡"),
                        "{\"icon\":\"pi pi-file-edit\"}",
                        2
                ),
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "系統與安全維運", "MENU_API_DOCS"),
                        SettingProfile.of("API 規格文件", "OpenAPI 3.0 與 Swagger 互動式 API 文件"),
                        "{\"icon\":\"pi pi-book\",\"url\":\"/swagger-ui.html\"}",
                        3
                )
        );

        for (Setting s : menus) {
            if (!settingJpaRepository.existsByScopeTenantAndScopeCategoryAndScopeCode(
                    s.getScope().getTenant(), s.getScope().getCategory(), s.getScope().getCode())) {
                settingJpaRepository.save(s);
                log.info("[SettingModule] Initialized Menu Item: {} ({}) for tenant '{}'",
                        s.getProfile().getName(), s.getScope().getCode(), s.getScope().getTenant());
            }
        }
    }
}
