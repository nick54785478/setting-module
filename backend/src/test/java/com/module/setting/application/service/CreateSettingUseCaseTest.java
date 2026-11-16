package com.module.setting.application.service;

import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.result.SettingCreatedResult;
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
 * 建立設定用例單元測試 (CreateSettingUseCaseTest)
 */
@ExtendWith(MockitoExtension.class)
class CreateSettingUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    private CreateSettingCommand sampleCommand;
    private SettingCreatedResult sampleResult;

    @BeforeEach
    void setUp() {
        sampleCommand = new CreateSettingCommand(
                "default",
                "FUNCTION",
                "DASHBOARD",
                "DASHBOARD_FUNCTION",
                "儀錶板",
                "Dashboard 相關功能",
                "Dashboard",
                1);

        sampleResult = new SettingCreatedResult(
                1L,
                "default",
                "FUNCTION",
                "DASHBOARD",
                "DASHBOARD_FUNCTION",
                "儀錶板",
                "Dashboard 相關功能",
                "Dashboard",
                1,
                "Y");
    }

    /**
     * Feature: 建立設定功能
     * Scenario: 成功建立新的系統設定
     * Given 提供合法且完整的 CreateSettingCommand (含多維坐標 category, subCategory)
     * When 呼叫 CreateSettingUseCase 的 create 方法
     * Then 委派 SettingRepositoryPort 儲存並回傳設定結果 DTO
     */
    @Test
    @DisplayName("成功建立設定")
    void shouldCreateSettingSuccessfully() {
        // Given
        when(settingRepositoryPort.save(any(CreateSettingCommand.class))).thenReturn(sampleResult);

        // When
        SettingCreatedResult result = settingApplicationService.create(sampleCommand);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("default", result.tenant());
        assertEquals("FUNCTION", result.category());
        assertEquals("DASHBOARD", result.subCategory());
        assertEquals("DASHBOARD_FUNCTION", result.code());
        assertEquals("儀錶板", result.name());
        assertEquals("Dashboard", result.value());
        assertEquals(1, result.priorityNo());
        assertEquals("Y", result.activeFlag());
        verify(settingRepositoryPort, times(1)).save(sampleCommand);
    }
}
