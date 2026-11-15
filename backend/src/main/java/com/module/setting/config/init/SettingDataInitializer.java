package com.module.setting.config.init;

import com.module.setting.application.port.in.InitTenantSettingUseCase;
import com.module.setting.infrastructure.persistence.category.entity.SettingCategory;
import com.module.setting.infrastructure.persistence.category.repository.SettingCategoryJpaRepository;
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
 * 1. 於 setting_category 表僅初始化全域唯一的 SYSTEM 基礎維度（移除其餘非 SYSTEM 資料）。
 * 2. setting 表不再自動注入預設業務假資料，保持乾淨。
 * 其餘維度與業務設定均由維運人員依照手動配置手冊於後台介面或 API 配置。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "setting.init.enabled", havingValue = "true", matchIfMissing = true)
public class SettingDataInitializer implements ApplicationRunner {

    private final InitTenantSettingUseCase initTenantSettingUseCase;
    private final SettingCategoryJpaRepository categoryJpaRepository;

    @Value("${setting.init.default-tenant:ALL}")
    private String defaultTenant;

    public SettingDataInitializer(
            InitTenantSettingUseCase initTenantSettingUseCase,
            SettingCategoryJpaRepository categoryJpaRepository) {
        this.initTenantSettingUseCase = initTenantSettingUseCase;
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("[SettingModule] Starting automated seed data initialization for default tenant: '{}'...", defaultTenant);
        try {
            // 1. 初始化 Meta Category 維度字典：僅保留 SYSTEM 作為唯一的全域基礎設施維度
            initCategories();

            // 2. 初始化基準業務功能設定 (setting 表，預設範本為空清單)
            int initializedCount = initTenantSettingUseCase.initDefaultSettings(defaultTenant);
            log.info("[SettingModule] Seed data initialization completed. Added {} new setting(s).", initializedCount);
        } catch (Exception e) {
            log.error("[SettingModule] Failed to initialize default settings for tenant '{}'", defaultTenant, e);
        }
    }

    private void initCategories() {
        // 僅保留 SYSTEM 維度，其餘維度 (ROLE, GROUP, FUNCTION) 全數由維運人員手動配置
        var categories = List.of(
                SettingCategory.of(defaultTenant, "SYSTEM", "系統配置", "系統底層與全域基礎設施配置維度", 1)
        );

        for (var cat : categories) {
            if (!categoryJpaRepository.existsByTenantAndCode(cat.getTenant(), cat.getCode())) {
                categoryJpaRepository.save(cat);
                log.info("[SettingModule] Initialized Meta Category: {} ({})", cat.getName(), cat.getCode());
            }
        }
    }
}
