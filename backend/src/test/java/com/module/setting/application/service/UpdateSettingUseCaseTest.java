package com.module.setting.application.service;

import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 更新設定用例單元測試 (UpdateSettingUseCaseTest)
 */
@ExtendWith(MockitoExtension.class)
class UpdateSettingUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    private UpdateSettingCommand updateCommand;
    private SettingUpdatedResult updatedResult;

    @BeforeEach
    void setUp() {
        updateCommand = new UpdateSettingCommand(
                1L,
                "FUNCTION",
                "DASHBOARD",
                "儀錶板(新版)",
                "更新 Dashboard 說明",
                "Dashboard_V2",
                2,
                "Y"
        );

        updatedResult = new SettingUpdatedResult(
                1L,
                "default",
                "FUNCTION",
                "DASHBOARD",
                "DASHBOARD_FUNCTION",
                "儀錶板(新版)",
                "更新 Dashboard 說明",
                "Dashboard_V2",
                2,
                "Y"
        );
    }

    /**
     * Feature: 更新設定功能
     * Scenario: 成功更新已存在的設定資料
     *   Given 提供合法且完整的 UpdateSettingCommand
     *   When 呼叫 UpdateSettingUseCase 的 update 方法
     *   Then 委派 SettingRepositoryPort 異動並回傳更新後的設定結果 DTO
     */
    @Test
    @DisplayName("成功更新設定")
    void shouldUpdateSettingSuccessfully() {
        // Given
        when(settingRepositoryPort.update(any(UpdateSettingCommand.class))).thenReturn(updatedResult);

        // When
        SettingUpdatedResult result = settingApplicationService.update(updateCommand);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Dashboard_V2", result.value());
        assertEquals("儀錶板(新版)", result.name());
        assertEquals(2, result.priorityNo());
        verify(settingRepositoryPort, times(1)).update(updateCommand);
    }

    /**
     * Feature: 更新設定功能
     * Scenario: 當更新不存在的設定 ID 時拋出例外
     *   Given 設定 ID 不存在於資料庫
     *   When 呼叫 UpdateSettingUseCase 的 update 方法
     *   Then 拋出 IllegalArgumentException 異常
     */
    @Test
    @DisplayName("更新不存在的設定時拋出異常")
    void shouldThrowExceptionWhenUpdatingNonExistentSetting() {
        // Given
        when(settingRepositoryPort.update(any(UpdateSettingCommand.class)))
                .thenThrow(new IllegalArgumentException("Setting not found with id: 999"));

        UpdateSettingCommand nonExistentCommand = new UpdateSettingCommand(
                999L, "FUNCTION", "DASHBOARD", "name", "desc", "val", 1, "Y");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                settingApplicationService.update(nonExistentCommand)
        );

        assertTrue(exception.getMessage().contains("Setting not found with id: 999"));
        verify(settingRepositoryPort, times(1)).update(nonExistentCommand);
    }

    @Test
    @DisplayName("停用預設語系時應拋出業務例外")
    void shouldThrowExceptionWhenDeactivatingDefaultLocale() {
        // Given: 原設定為 DEFAULT 語系
        SettingGottenResult defaultLocale = new SettingGottenResult(
                10L, "CW", "LOCALE", "UI", "zh-TW", "繁體中文", "說明", "DEFAULT", 1, "Y"
        );
        when(settingRepositoryPort.findById(10L)).thenReturn(Optional.of(defaultLocale));

        // When: 嘗試將 activeFlag 更新為 N (停用)
        UpdateSettingCommand deactivateCommand = new UpdateSettingCommand(
                10L, "LOCALE", "UI", "繁體中文", "說明", "DEFAULT", 1, "N"
        );

        // Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                settingApplicationService.update(deactivateCommand)
        );
        assertTrue(ex.getMessage().contains("租戶預設語系 (DEFAULT) 正在使用中，不得停用"));
        verify(settingRepositoryPort, never()).update(any());
    }
}
