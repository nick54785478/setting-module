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
 * 遵循 Function Configurable 設計架構，於 Spring Boot 應用程式啟動完成後自動執行種子資料初始化：
 * <ol>
 *   <li><b>元資料維度字典 (SettingCategory)</b>：初始化 SYSTEM (系統配置)、LOCALE (語系配置)、MENU (選單配置) 與 DYNAMIC_PAGE (動態頁面配置)。</li>
 *   <li><b>基準國際化語系 (Baseline Locales)</b>：為預設租戶 (預設為 CW) 注入繁體中文 (zh-TW, 設為 DEFAULT)、英文 (en-US)、日文 (ja-JP)。</li>
 *   <li><b>側邊欄動態選單 (Sidebar Menus)</b>：配置兩大功能群組（核心業務配置、系統與安全維運），並具備自我修補機制 (Self-Healing) 以相容歷史舊資料。</li>
 *   <li><b>基準業務功能設定 (Default Settings)</b>：調用業務 UseCase 初始化租戶的核心功能參數與開關。</li>
 * </ol>
 * <p>
 * 可透過設定檔屬性 {@code setting.init.enabled=false} 關閉本初始化執行器；
 * 透過 {@code setting.init.default-tenant=CW} 指定種子資料注入之預設租戶。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "setting.init.enabled", havingValue = "true", matchIfMissing = true)
public class SettingDataInitializer implements ApplicationRunner {

    /** 租戶基準功能設定初始化業務案例 */
    private final InitTenantSettingUseCase initTenantSettingUseCase;

    /** 維度元資料字典 JPA 倉儲 */
    private final SettingCategoryJpaRepository categoryJpaRepository;

    /** 業務功能設定 JPA 倉儲 */
    private final SettingJpaRepository settingJpaRepository;

    /** 預設初始化的目標租戶識別代碼 (由 application.yml 注入，預設為 "CW") */
    @Value("${setting.init.default-tenant:CW}")
    private String defaultTenant;

    /**
     * 建構子注入相依元件
     *
     * @param initTenantSettingUseCase 租戶設定初始化 UseCase
     * @param categoryJpaRepository    維度字典倉儲
     * @param settingJpaRepository     設定項目倉儲
     */
    public SettingDataInitializer(
            InitTenantSettingUseCase initTenantSettingUseCase,
            SettingCategoryJpaRepository categoryJpaRepository,
            SettingJpaRepository settingJpaRepository) {
        this.initTenantSettingUseCase = initTenantSettingUseCase;
        this.categoryJpaRepository = categoryJpaRepository;
        this.settingJpaRepository = settingJpaRepository;
    }

    /**
     * 應用程式啟動完成後之回呼入口 (ApplicationRunner 實作)
     * <p>
     * 循序執行四大初始化任務：維度字典 -> 基準語系 -> 側邊導航選單 -> 基準功能設定。
     * 若過程中發生非預期例外，將記錄 ERROR 等級日誌，避免阻斷主程式啟動流程。
     * </p>
     *
     * @param args 應用程式啟動傳入參數
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("[SettingModule] Starting automated seed data initialization for default tenant: '{}'...", defaultTenant);
        try {
            // 1. 初始化 Meta Category 維度字典：SYSTEM 基礎維度、LOCALE 語系維度、MENU 選單維度與 DYNAMIC_PAGE 動態頁面維度
            initCategories();

            // 2. 初始化基準語系配置 (zh-TW [預設], en-US, ja-JP)
            initBaselineLocales();

            // 3. 初始化側邊欄導航選單配置 (包含核心業務配置、系統與安全維運，並具備舊資料自我修補)
            initBaselineMenus();

            // 4. 初始化基準業務功能設定 (呼叫應用層 UseCase 注入預設業務開關與參數)
            int initializedCount = initTenantSettingUseCase.initDefaultSettings(defaultTenant);
            log.info("[SettingModule] Seed data initialization completed. Added {} new setting(s).", initializedCount);
        } catch (Exception e) {
            log.error("[SettingModule] Failed to initialize default settings for tenant '{}'", defaultTenant, e);
        }
    }

    /**
     * 初始化系統維度元資料字典 (SettingCategory Meta 資料)
     * <p>
     * 於 {@code setting_category} 表中為預設租戶建立四個基礎核心維度：
     * <ul>
     *   <li><b>SYSTEM (系統配置)</b>：系統底層與全域基礎設施配置維度 (priorityNo = 1)。</li>
     *   <li><b>LOCALE (系統語系配置)</b>：管理各租戶支援的系統語言環境與國際化設定 (priorityNo = 2)。</li>
     *   <li><b>MENU (側邊欄選單配置)</b>：管理側邊導航列之分組、項目、圖示、路由與顯示順序 (priorityNo = 3)。</li>
     *   <li><b>DYNAMIC_PAGE (動態頁面配置)</b>：管理各租戶專屬自訂動態頁面 Schema 與表單填寫資料 (priorityNo = 4)。</li>
     * </ul>
     * 本方法具備冪等性保證 (Idempotent)，若該維度字典已存在則略過不覆寫。
     * </p>
     */
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

    /**
     * 初始化基準國際化語系資料 (Baseline Locales)
     * <p>
     * 遵循 Function Configurable 多租戶隔離原則，每個 Tenant 獨立維護專屬語系清單，不使用全域 ALL 語系。
     * 本方法為預設展示租戶 (CW) 提供開箱即用之標準三國語言環境：
     * <ul>
     *   <li><b>zh-TW (繁體中文 - 台灣)</b>：標記 value 為 {@code DEFAULT}，作為系統預設首選語系 (priorityNo = 1)。</li>
     *   <li><b>en-US (English - United States)</b>：美式英語 (priorityNo = 2)。</li>
     *   <li><b>ja-JP (日本語 - 日本)</b>：日語 (priorityNo = 3)。</li>
     * </ul>
     * 透過 {@code existsByScopeTenantAndScopeCategoryAndScopeCode} 確保多次啟動時不會重複插入。
     * </p>
     */
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

    /**
     * 初始化側邊欄導航動態選單配置 (Baseline Menus)
     * <p>
     * 側邊選單依據 {@code category=MENU} 動態檢索，以 {@code subCategory} 作為一級分組，
     * 並由 {@code priorityNo} 決定排序。各項目在 {@code value} 中存放 JSON 格式之圖示、徽章與視圖代碼：
     * <br>
     * <b>分組一：核心業務配置</b>
     * <ul>
     *   <li><b>MENU_SETTING (功能設定管理)</b>：view = "setting", icon = "pi pi-sliders-h", badge = "核心" (順序 1)</li>
     *   <li><b>MENU_CATEGORY (元資料字典)</b>：view = "category", icon = "pi pi-database", badge = "元資料" (順序 2)</li>
     *   <li><b>MENU_LOCALE (系統語系設定)</b>：view = "locale", icon = "pi pi-globe", badge = "語系" (順序 3)</li>
     *   <li><b>MENU_DESIGNER (動態表單設計)</b>：view = "dynamic-designer", icon = "pi pi-pencil", badge = "設計" (順序 4)</li>
     *   <li><b>MENU_TENANT (租戶畫像資料)</b>：view = "tenant-profile", icon = "pi pi-building" (順序 5)</li>
     * </ul>
     * <b>分組二：系統與安全維運</b>
     * <ul>
     *   <li><b>MENU_ROLE (系統權限角色)</b>：icon = "pi pi-shield" (順序 1)</li>
     *   <li><b>MENU_AUDIT (操作稽核日誌)</b>：icon = "pi pi-file-edit" (順序 2)</li>
     *   <li><b>MENU_API_DOCS (API 規格文件)</b>：icon = "pi pi-book", url = "/swagger-ui.html" (順序 3)</li>
     * </ul>
     * <p>
     * <b>自我修復與相容機制 (Self-Healing)</b>：<br>
     * 若資料庫中已存在舊紀錄但缺乏 {@code "view"} 屬性，或歷史紀錄名稱仍為舊版「租戶隔離中心」，
     * 本方法將自動更新其 {@code value} 與 {@code name}，無痛相容舊有環境，免手動下 SQL 修正。
     * </p>
     */
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
                        SettingScope.of(defaultTenant, "MENU", "核心業務配置", "MENU_LOCALE"),
                        SettingProfile.of("系統語系設定", "多租戶支援之國際化語系維護與預設值配置"),
                        "{\"icon\":\"pi pi-globe\",\"badge\":\"語系\",\"view\":\"locale\"}",
                        3
                ),
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "核心業務配置", "MENU_DESIGNER"),
                        SettingProfile.of("動態表單設計", "視覺化自訂租戶動態表單欄位與配置"),
                        "{\"icon\":\"pi pi-pencil\",\"badge\":\"設計\",\"view\":\"dynamic-designer\"}",
                        4
                ),
                Setting.create(
                        SettingScope.of(defaultTenant, "MENU", "核心業務配置", "MENU_TENANT"),
                        SettingProfile.of("租戶畫像資料", "多租戶專屬自訂畫像資料與動態表單配置"),
                        "{\"icon\":\"pi pi-building\",\"view\":\"tenant-profile\"}",
                        5
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
            var existingOpt = settingJpaRepository.findByScopeTenantAndScopeCodeAndActiveFlag(
                    s.getScope().getTenant(), s.getScope().getCode(), com.module.setting.application.shared.vo.YesNo.Y);
            if (existingOpt.isPresent()) {
                Setting existing = existingOpt.get();
                boolean needUpdate = false;
                // 1. 若資料庫中的舊紀錄缺乏 "view" 路由配置，標記需要自動補齊
                if (existing.getValue() == null || !existing.getValue().contains("\"view\"")) {
                    needUpdate = true;
                }
                // 2. 若 MENU_TENANT 歷史名稱仍為舊版「租戶隔離中心」，自動更新為最新「租戶畫像資料」
                if ("MENU_TENANT".equals(s.getScope().getCode()) && "租戶隔離中心".equals(existing.getProfile().getName())) {
                    needUpdate = true;
                }
                // 執行自我修補更新
                if (needUpdate) {
                    existing.update(
                            existing.getScope(),
                            s.getProfile(),
                            s.getValue(),
                            existing.getPriorityNo(),
                            existing.getActiveFlag().name()
                    );
                    settingJpaRepository.save(existing);
                    log.info("[SettingModule] Updated Menu Item: {} ({}) with view configuration",
                            s.getProfile().getName(), s.getScope().getCode());
                }
            } else {
                // 資料庫無此選單項目，直接寫入新種子紀錄
                settingJpaRepository.save(s);
                log.info("[SettingModule] Initialized Menu Item: {} ({}) for tenant '{}'",
                        s.getProfile().getName(), s.getScope().getCode(), s.getScope().getTenant());
            }
        }
    }
}
