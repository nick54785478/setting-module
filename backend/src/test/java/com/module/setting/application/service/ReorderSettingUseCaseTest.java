package com.module.setting.application.service;

import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.command.ReorderSettingItemCommand;
import com.module.setting.application.shared.result.SettingGottenResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReorderSettingUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    @Test
    @DisplayName("批次排序設定項目 - 成功呼叫 Repository 並回傳依 priorityNo 升冪排序之清單")
    void shouldReorderSettingsSuccessfully() {
        // Arrange
        List<ReorderSettingItemCommand> commands = List.of(
                new ReorderSettingItemCommand(10L, 1),
                new ReorderSettingItemCommand(9L, 2),
                new ReorderSettingItemCommand(11L, 3)
        );

        List<SettingGottenResult> sortedResults = List.of(
                new SettingGottenResult(10L, "CW", "MENU", "核心業務配置", "MENU_CATEGORY", "元資料字典", "", "{}", 1, "Y"),
                new SettingGottenResult(9L, "CW", "MENU", "核心業務配置", "MENU_SETTING", "功能設定管理", "", "{}", 2, "Y"),
                new SettingGottenResult(11L, "CW", "MENU", "核心業務配置", "MENU_TENANT", "租戶隔離中心", "", "{}", 3, "Y")
        );

        when(settingRepositoryPort.reorder(commands)).thenReturn(sortedResults);

        // Act
        List<SettingGottenResult> actual = settingApplicationService.reorder(commands);

        // Assert
        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).id()).isEqualTo(10L);
        assertThat(actual.get(0).priorityNo()).isEqualTo(1);
        assertThat(actual.get(1).id()).isEqualTo(9L);
        assertThat(actual.get(1).priorityNo()).isEqualTo(2);
        assertThat(actual.get(2).id()).isEqualTo(11L);
        assertThat(actual.get(2).priorityNo()).isEqualTo(3);

        verify(settingRepositoryPort, times(1)).reorder(commands);
    }

    @Test
    @DisplayName("空指令清單 - 直接回傳空清單而不呼叫 Repository")
    void shouldReturnEmptyListWhenCommandsAreEmpty() {
        // Act
        List<SettingGottenResult> actual = settingApplicationService.reorder(List.of());

        // Assert
        assertThat(actual).isEmpty();
        verify(settingRepositoryPort, never()).reorder(any());
    }
}
