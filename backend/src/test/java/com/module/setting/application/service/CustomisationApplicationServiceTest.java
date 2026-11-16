package com.module.setting.application.service;

import com.module.setting.application.port.out.CustomisationRepositoryPort;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.infrastructure.persistence.customisation.entity.Customisation;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomisationApplicationServiceTest {

    @Mock
    private CustomisationRepositoryPort repositoryPort;

    private CustomisationApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new CustomisationApplicationService(repositoryPort);
        UserContextHolder.set("CW", "test.user@cw.com");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("查詢個人化配置：若資料庫無紀錄，應回傳空清單")
    void testGetCustomisation_NotFound_ReturnsEmptyList() {
        when(repositoryPort.findByScope(any())).thenReturn(Optional.empty());

        List<String> results = applicationService.getCustomisation("SettingComponent", "FieldView");

        assertThat(results).isEmpty();
        verify(repositoryPort).findByScope(CustomisationScope.of("CW", "test.user@cw.com", "SettingComponent", "FieldView"));
    }

    @Test
    @DisplayName("查詢個人化配置：若有紀錄，應正確以逗號分隔解析出欄位清單")
    void testGetCustomisation_Found_ReturnsParsedList() {
        String csv = "tenant,category,code,name,value";
        CustomisationScope scope = CustomisationScope.of("CW", "test.user@cw.com", "SettingComponent", "FieldView");
        Customisation customisation = Customisation.create(scope, csv);

        when(repositoryPort.findByScope(scope)).thenReturn(Optional.of(customisation));

        List<String> results = applicationService.getCustomisation("SettingComponent", "FieldView");

        assertThat(results).containsExactly("tenant", "category", "code", "name", "value");
    }

    @Test
    @DisplayName("儲存個人化配置：應將欄位清單拼接為逗號分隔字串存入")
    void testSaveCustomisation_SavesAsCommaSeparatedString() {
        List<String> fields = List.of("code", "name", "value");

        when(repositoryPort.findByScope(any())).thenReturn(Optional.empty());

        List<String> result = applicationService.saveCustomisation("SettingComponent", "FieldView", fields);

        assertThat(result).containsExactly("code", "name", "value");
        ArgumentCaptor<Customisation> captor = ArgumentCaptor.forClass(Customisation.class);
        verify(repositoryPort).save(captor.capture());

        Customisation saved = captor.getValue();
        assertThat(saved.getScope().getTenant()).isEqualTo("CW");
        assertThat(saved.getScope().getUsername()).isEqualTo("test.user@cw.com");
        assertThat(saved.getScope().getComponent()).isEqualTo("SettingComponent");
        assertThat(saved.getScope().getType()).isEqualTo("FieldView");
        assertThat(saved.getValue()).isEqualTo("code,name,value");
    }

    @Test
    @DisplayName("還原預設值：應呼叫 repositoryPort 依據 scope 刪除")
    void testResetCustomisation_CallsDelete() {
        applicationService.resetCustomisation("SettingComponent", "FieldView");

        verify(repositoryPort).deleteByScope(CustomisationScope.of("CW", "test.user@cw.com", "SettingComponent", "FieldView"));
    }
}
