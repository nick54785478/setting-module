package com.module.setting.application.service;

import com.module.setting.application.port.out.CustomisationRepositoryPort;
import com.module.setting.application.shared.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        when(repositoryPort.findActiveValue(any(), any(), any(), any())).thenReturn(Optional.empty());

        List<String> results = applicationService.getCustomisation("SettingComponent", "FieldView");

        assertThat(results).isEmpty();
        verify(repositoryPort).findActiveValue("CW", "test.user@cw.com", "SettingComponent", "FieldView");
    }

    @Test
    @DisplayName("查詢個人化配置：若有紀錄，應正確以逗號分隔解析出欄位清單")
    void testGetCustomisation_Found_ReturnsParsedList() {
        String csv = "tenant,category,code,name,value";
        when(repositoryPort.findActiveValue("CW", "test.user@cw.com", "SettingComponent", "FieldView"))
                .thenReturn(Optional.of(csv));

        List<String> results = applicationService.getCustomisation("SettingComponent", "FieldView");

        assertThat(results).containsExactly("tenant", "category", "code", "name", "value");
    }

    @Test
    @DisplayName("儲存個人化配置：應將欄位清單拼接為逗號分隔字串傳予 port 儲存")
    void testSaveCustomisation_SavesAsCommaSeparatedString() {
        List<String> fields = List.of("code", "name", "value");

        List<String> result = applicationService.saveCustomisation("SettingComponent", "FieldView", fields);

        assertThat(result).containsExactly("code", "name", "value");
        verify(repositoryPort).save("CW", "test.user@cw.com", "SettingComponent", "FieldView", "code,name,value");
    }

    @Test
    @DisplayName("還原預設值：應呼叫 repositoryPort 依據坐標刪除")
    void testResetCustomisation_CallsDelete() {
        applicationService.resetCustomisation("SettingComponent", "FieldView");

        verify(repositoryPort).delete("CW", "test.user@cw.com", "SettingComponent", "FieldView");
    }
}
