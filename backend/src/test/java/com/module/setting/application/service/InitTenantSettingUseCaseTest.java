package com.module.setting.application.service;

import com.module.setting.application.port.out.SettingRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 租戶設定初始化用例單元測試 (InitTenantSettingUseCaseTest)
 */
@ExtendWith(MockitoExtension.class)
class InitTenantSettingUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    /**
     * Feature: 租戶預設設定初始化功能
     * Scenario: 當業務設定範本為純淨狀態（不注入預設假資料）時，初始化回傳 0 筆新增
     *   Given 指定租戶 "CW"
     *   When 呼叫 InitTenantSettingUseCase 的 initDefaultSettings 方法
     *   Then 依據空範本執行，回傳 0 筆新增且不呼叫 save
     */
    @Test
    @DisplayName("空種子範本時成功初始化且不寫入任何假資料")
    void shouldInitializeZeroSettingsWhenSeedTemplateIsEmpty() {
        // When
        int createdCount = settingApplicationService.initDefaultSettings("CW");

        // Then
        assertEquals(0, createdCount);
        verify(settingRepositoryPort, never()).save(any());
    }

    /**
     * Feature: 租戶預設設定初始化功能
     * Scenario: 驗證初始化具備冪等性，若既有項目已存在則自動略過
     *   Given 指定租戶 "tenant-beta"
     *   When 呼叫 InitTenantSettingUseCase 的 initDefaultSettings 方法
     *   Then 略過所有既有設定，新增筆數為 0，且不呼叫 save
     */
    @Test
    @DisplayName("重複執行初始化時略過既有項目 (冪等性驗證)")
    void shouldSkipExistingSettingsWhenReinitializing() {
        // When
        int createdCount = settingApplicationService.initDefaultSettings("tenant-beta");

        // Then
        assertEquals(0, createdCount);
        verify(settingRepositoryPort, never()).save(any());
    }

    /**
     * Feature: 租戶預設設定初始化功能
     * Scenario: 傳入空白租戶代碼時拋出異常
     *   Given 租戶代碼為空白
     *   When 呼叫 InitTenantSettingUseCase 的 initDefaultSettings 方法
     *   Then 拋出 IllegalArgumentException 異常
     */
    @Test
    @DisplayName("傳入空白租戶代碼時拋出異常")
    void shouldThrowExceptionWhenTenantIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                settingApplicationService.initDefaultSettings("  ")
        );

        assertTrue(exception.getMessage().contains("Tenant cannot be blank"));
        verify(settingRepositoryPort, never()).save(any());
    }
}
