package com.module.setting.presentation.controller;

import com.module.setting.application.port.in.*;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import com.module.setting.presentation.assembler.SettingResourceAssembler;
import com.module.setting.presentation.resource.out.TenantLocaleOptionGottenResource;
import com.module.setting.presentation.resource.out.TenantMenuItemGottenResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingControllerLocaleTest {

    @Mock
    private CreateSettingUseCase createSettingUseCase;
    @Mock
    private UpdateSettingUseCase updateSettingUseCase;
    @Mock
    private GetSettingUseCase getSettingUseCase;
    @Mock
    private GetSettingPagedUseCase getSettingPagedUseCase;
    @Mock
    private GetSettingOptionUseCase getSettingOptionUseCase;
    @Mock
    private DeleteSettingUseCase deleteSettingUseCase;
    @Mock
    private ReorderSettingUseCase reorderSettingUseCase;
    @Mock
    private SettingResourceAssembler settingResourceAssembler;

    @InjectMocks
    private SettingController settingController;

    @Test
    @DisplayName("取得指定租戶已啟用之語系列表 - 成功")
    void shouldGetTenantLocales() {
        // Arrange
        List<SettingGottenResult> mockLocales = List.of(
                new SettingGottenResult(1L, "CW", "LOCALE", "UI", "zh-TW", "繁體中文", "繁體中文說明", "DEFAULT", 1, "Y"),
                new SettingGottenResult(2L, "CW", "LOCALE", "UI", "en-US", "English", "English desc", "", 2, "Y")
        );
        when(getSettingUseCase.getByCategory("CW", "LOCALE")).thenReturn(mockLocales);

        // Act
        ResponseEntity<List<TenantLocaleOptionGottenResource>> response = settingController.getTenantLocales("CW");

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).code()).isEqualTo("zh-TW");
        assertThat(response.getBody().get(0).isDefault()).isTrue();
        assertThat(response.getBody().get(1).code()).isEqualTo("en-US");
        assertThat(response.getBody().get(1).isDefault()).isFalse();
    }

    @Test
    @DisplayName("租戶尚未配置專屬語系時回傳空清單 (不回退至全域 ALL 語系) - 成功")
    void shouldReturnEmptyListWhenTenantHasNoLocales() {
        // Arrange
        when(getSettingUseCase.getByCategory("NEW_TENANT", "LOCALE")).thenReturn(List.of());

        // Act
        ResponseEntity<List<TenantLocaleOptionGottenResource>> response = settingController.getTenantLocales("NEW_TENANT");

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
        verify(getSettingUseCase, times(1)).getByCategory("NEW_TENANT", "LOCALE");
        verify(getSettingUseCase, never()).getByCategory(eq("ALL"), anyString());
    }

    @Test
    @DisplayName("設定指定語系為租戶預設語系 - 成功")
    void shouldSetLocaleDefault() {
        // Arrange
        Long targetId = 2L;
        SettingGottenResult target = new SettingGottenResult(
                targetId, "CW", "LOCALE", "UI", "en-US", "English", "English desc", "", 2, "Y");
        SettingGottenResult oldDefault = new SettingGottenResult(
                1L, "CW", "LOCALE", "UI", "zh-TW", "繁體中文", "繁體中文說明", "DEFAULT", 1, "Y");

        when(getSettingUseCase.getById(targetId)).thenReturn(target);
        when(getSettingUseCase.getByCategory("CW", "LOCALE")).thenReturn(List.of(oldDefault, target));

        SettingUpdatedResult updatedResult = new SettingUpdatedResult(
                targetId, "CW", "LOCALE", "UI", "en-US", "English", "English desc", "DEFAULT", 2, "Y");
        when(updateSettingUseCase.update(any(UpdateSettingCommand.class))).thenReturn(updatedResult);

        // Act
        ResponseEntity<TenantLocaleOptionGottenResource> response = settingController.setLocaleDefault(targetId);

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isDefault()).isTrue();
        verify(updateSettingUseCase, times(2)).update(any(UpdateSettingCommand.class));
    }

    @Test
    @DisplayName("取得指定租戶已啟用之側邊欄選單清單 - 成功")
    void shouldGetTenantMenus() {
        // Arrange
        List<SettingGottenResult> mockMenus = List.of(
                new SettingGottenResult(101L, "CW", "MENU", "核心業務配置", "MENU_SETTING", "功能設定管理", "說明",
                        "{\"icon\":\"pi pi-sliders-h\",\"badge\":\"核心\",\"view\":\"setting\"}", 1, "Y"),
                new SettingGottenResult(102L, "CW", "MENU", "系統與安全維運", "MENU_API_DOCS", "API 規格文件", "說明",
                        "{\"icon\":\"pi pi-book\",\"url\":\"/swagger-ui.html\"}", 2, "Y")
        );
        when(getSettingUseCase.getByCategory("CW", "MENU")).thenReturn(mockMenus);
        when(settingResourceAssembler.toMenuItemResource(any())).thenAnswer(inv -> {
            SettingGottenResult r = inv.getArgument(0);
            return new TenantMenuItemGottenResource(r.id(), r.tenant(), r.subCategory(), r.code(), r.name(), r.description(), r.value(), r.priorityNo(), r.activeFlag());
        });

        // Act
        ResponseEntity<List<TenantMenuItemGottenResource>> response = settingController.getTenantMenus("CW");

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).code()).isEqualTo("MENU_SETTING");
        assertThat(response.getBody().get(0).group()).isEqualTo("核心業務配置");
        assertThat(response.getBody().get(1).code()).isEqualTo("MENU_API_DOCS");
        assertThat(response.getBody().get(1).group()).isEqualTo("系統與安全維運");
    }

    @Test
    @DisplayName("批次排序設定項目 - 成功呼叫並回傳依 priorityNo 排序之 SettingGottenResource")
    void shouldReorderSettings() {
        // Arrange
        var requestItems = List.of(
                new com.module.setting.presentation.resource.in.ReorderSettingItemResource(10L, 1),
                new com.module.setting.presentation.resource.in.ReorderSettingItemResource(9L, 2)
        );
        var mockCommands = List.of(
                new com.module.setting.application.shared.command.ReorderSettingItemCommand(10L, 1),
                new com.module.setting.application.shared.command.ReorderSettingItemCommand(9L, 2)
        );
        var mockResults = List.of(
                new SettingGottenResult(10L, "CW", "MENU", "核心業務配置", "MENU_CATEGORY", "元資料字典", "", "{}", 1, "Y"),
                new SettingGottenResult(9L, "CW", "MENU", "核心業務配置", "MENU_SETTING", "功能設定管理", "", "{}", 2, "Y")
        );

        when(settingResourceAssembler.toReorderCommands(requestItems)).thenReturn(mockCommands);
        when(reorderSettingUseCase.reorder(mockCommands)).thenReturn(mockResults);
        when(settingResourceAssembler.toGottenResource(any())).thenAnswer(inv -> {
            SettingGottenResult r = inv.getArgument(0);
            return new com.module.setting.presentation.resource.out.SettingGottenResource(
                    r.id(), r.tenant(), r.category(), r.subCategory(), r.code(), r.name(), r.description(), r.value(), r.priorityNo(), r.activeFlag()
            );
        });

        // Act
        var response = settingController.reorderSettings(requestItems);

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(10L);
        assertThat(response.getBody().get(0).priorityNo()).isEqualTo(1);
        assertThat(response.getBody().get(1).id()).isEqualTo(9L);
        assertThat(response.getBody().get(1).priorityNo()).isEqualTo(2);
    }

    @Test
    @DisplayName("批次排序選單項目 - 成功呼叫並回傳依 priorityNo 排序之 TenantMenuItemGottenResource")
    void shouldReorderMenus() {
        // Arrange
        var requestItems = List.of(
                new com.module.setting.presentation.resource.in.ReorderSettingItemResource(10L, 1),
                new com.module.setting.presentation.resource.in.ReorderSettingItemResource(9L, 2)
        );
        var mockCommands = List.of(
                new com.module.setting.application.shared.command.ReorderSettingItemCommand(10L, 1),
                new com.module.setting.application.shared.command.ReorderSettingItemCommand(9L, 2)
        );
        var mockResults = List.of(
                new SettingGottenResult(10L, "CW", "MENU", "核心業務配置", "MENU_CATEGORY", "元資料字典", "", "{}", 1, "Y"),
                new SettingGottenResult(9L, "CW", "MENU", "核心業務配置", "MENU_SETTING", "功能設定管理", "", "{}", 2, "Y")
        );

        when(settingResourceAssembler.toReorderCommands(requestItems)).thenReturn(mockCommands);
        when(reorderSettingUseCase.reorder(mockCommands)).thenReturn(mockResults);
        when(settingResourceAssembler.toMenuItemResource(any())).thenAnswer(inv -> {
            SettingGottenResult r = inv.getArgument(0);
            return new TenantMenuItemGottenResource(r.id(), r.tenant(), r.subCategory(), r.code(), r.name(), r.description(), r.value(), r.priorityNo(), r.activeFlag());
        });

        // Act
        var response = settingController.reorderMenus(requestItems);

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(10L);
        assertThat(response.getBody().get(0).priorityNo()).isEqualTo(1);
        assertThat(response.getBody().get(1).id()).isEqualTo(9L);
        assertThat(response.getBody().get(1).priorityNo()).isEqualTo(2);
    }
}
