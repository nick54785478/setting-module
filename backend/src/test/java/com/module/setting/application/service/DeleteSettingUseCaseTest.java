package com.module.setting.application.service;

import com.module.setting.application.shared.command.DeleteSettingCommand;
import com.module.setting.application.port.out.SettingRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * 刪除設定用例單元測試 (DeleteSettingUseCaseTest)
 */
@ExtendWith(MockitoExtension.class)
class DeleteSettingUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    /**
     * Feature: 刪除設定功能
     * Scenario: 成功邏輯刪除指定 ID 之設定
     *   Given 指定合法的設定 ID
     *   When 呼叫 DeleteSettingUseCase 的 delete 方法
     *   Then 委派 SettingRepositoryPort 執行 deleteById 操作
     */
    @Test
    @DisplayName("成功執行刪除設定")
    void shouldDeleteSettingSuccessfully() {
        // Given
        DeleteSettingCommand command = new DeleteSettingCommand(1L);
        doNothing().when(settingRepositoryPort).deleteById(1L);

        // When
        settingApplicationService.delete(command);

        // Then
        verify(settingRepositoryPort, times(1)).deleteById(1L);
    }
}
