package com.module.setting.application.service;

import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 下拉選單選項用例單元測試 (GetSettingOptionUseCaseTest)
 */
@ExtendWith(MockitoExtension.class)
class GetSettingOptionUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    private SettingOptionGottenResult categoryOption;
    private SettingOptionGottenResult subCategoryOption;

    @BeforeEach
    void setUp() {
        categoryOption = new SettingOptionGottenResult("FUNCTION", "功能配置");
        subCategoryOption = new SettingOptionGottenResult("DASHBOARD_FUNCTION", "儀錶板");
    }

    /**
     * Feature: 下拉選單選項查詢功能
     * Scenario: 成功取得所有 Category 資料清單 (供前端下拉選單綁定)
     *   Given 指定或預設之租戶代碼
     *   When 呼叫 GetSettingOptionUseCase 的 getCategoryOptions 方法
     *   Then 委派 SettingRepositoryPort 查詢並回傳對應的 Category 選項清單 (name = option.label, code = option.value)
     */
    @Test
    @DisplayName("成功取得所有 Category 下拉選單選項")
    void shouldGetCategoryOptionsSuccessfully() {
        // Given
        when(settingRepositoryPort.findCategoryOptions("default"))
                .thenReturn(List.of(categoryOption));

        // When
        List<SettingOptionGottenResult> results = settingApplicationService.getCategoryOptions("default");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("FUNCTION", results.get(0).code());
        assertEquals("功能配置", results.get(0).name());
        assertEquals("功能配置", results.get(0).label());
        assertEquals("FUNCTION", results.get(0).value());
        verify(settingRepositoryPort, times(1)).findCategoryOptions("default");
    }

    /**
     * Feature: 下拉選單選項查詢功能
     * Scenario: 透過 Category 成功取得相關 SubCategory 下拉選單選項
     *   Given 指定合法之 Category (例如: "FUNCTION") 與租戶代碼
     *   When 呼叫 GetSettingOptionUseCase 的 getSubCategoryOptions 方法
     *   Then 委派 SettingRepositoryPort 查詢並回傳 SubCategory 選項清單 (name = option.label, code = option.value)
     */
    @Test
    @DisplayName("透過 Category 成功取得相關 SubCategory 下拉選項")
    void shouldGetSubCategoryOptionsSuccessfully() {
        // Given
        when(settingRepositoryPort.findSubCategoryOptions("default", "FUNCTION"))
                .thenReturn(List.of(subCategoryOption));

        // When
        List<SettingOptionGottenResult> results = settingApplicationService.getSubCategoryOptions("default", "FUNCTION");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("DASHBOARD_FUNCTION", results.get(0).code());
        assertEquals("儀錶板", results.get(0).name());
        assertEquals("儀錶板", results.get(0).label());
        assertEquals("DASHBOARD_FUNCTION", results.get(0).value());
        verify(settingRepositoryPort, times(1)).findSubCategoryOptions("default", "FUNCTION");
    }

    /**
     * Feature: 下拉選單選項查詢功能
     * Scenario: 查詢 SubCategory 時傳入空白之 Category 應拋出異常
     *   Given Category 為空字串
     *   When 呼叫 GetSettingOptionUseCase 的 getSubCategoryOptions 方法
     *   Then 拋出 IllegalArgumentException 異常
     */
    @Test
    @DisplayName("查詢 SubCategory 時 Category 為空拋出異常")
    void shouldThrowExceptionWhenCategoryIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                settingApplicationService.getSubCategoryOptions("default", "  ")
        );

        assertTrue(exception.getMessage().contains("Category cannot be blank"));
        verify(settingRepositoryPort, never()).findSubCategoryOptions(any(), any());
    }
}
