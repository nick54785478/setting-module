package com.module.setting.application.service;

import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.port.out.SettingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 查詢設定用例單元測試 (GetSettingUseCaseTest)
 */
@ExtendWith(MockitoExtension.class)
class GetSettingUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    private SettingGottenResult sampleResult;

    @BeforeEach
    void setUp() {
        sampleResult = new SettingGottenResult(
                1L,
                "default",
                "FUNCTION",
                "DASHBOARD",
                "DASHBOARD_FUNCTION",
                "儀錶板",
                "Dashboard 相關功能",
                "Dashboard",
                1,
                "Y"
        );
    }

    /**
     * Feature: 依 ID 查詢設定功能
     * Scenario: 成功查詢已存在的設定
     *   Given 設定 ID 存在
     *   When 呼叫 GetSettingUseCase 的 getById 方法
     *   Then 回傳對應之 SettingGottenResult
     */
    @Test
    @DisplayName("依 ID 成功查詢設定")
    void shouldGetSettingByIdSuccessfully() {
        // Given
        when(settingRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleResult));

        // When
        SettingGottenResult result = settingApplicationService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("default", result.tenant());
        assertEquals("FUNCTION", result.category());
        verify(settingRepositoryPort, times(1)).findById(1L);
    }

    /**
     * Feature: 依 ID 查詢設定功能
     * Scenario: 查詢不存在的設定 ID 時拋出例外
     *   Given 設定 ID 不存在
     *   When 呼叫 GetSettingUseCase 的 getById 方法
     *   Then 拋出 IllegalArgumentException 異常
     */
    @Test
    @DisplayName("查詢不存在之 ID 拋出異常")
    void shouldThrowExceptionWhenIdNotFound() {
        // Given
        when(settingRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                settingApplicationService.getById(999L)
        );

        assertTrue(exception.getMessage().contains("Setting not found with id: 999"));
        verify(settingRepositoryPort, times(1)).findById(999L);
    }

    /**
     * Feature: 依大分類 (Category) 查詢設定功能
     * Scenario: 查詢特定租戶下所有 FUNCTION 設定清單
     *   Given 指定租戶 default 與 category FUNCTION
     *   When 呼叫 GetSettingUseCase 的 getByCategory 方法
     *   Then 回傳符合該分類的設定清單
     */
    @Test
    @DisplayName("依功能大維度查詢設定清單")
    void shouldGetSettingsByCategorySuccessfully() {
        // Given
        when(settingRepositoryPort.findByCategory("default", "FUNCTION"))
                .thenReturn(List.of(sampleResult));

        // When
        List<SettingGottenResult> results = settingApplicationService.getByCategory("default", "FUNCTION");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("FUNCTION", results.get(0).category());
        verify(settingRepositoryPort, times(1)).findByCategory("default", "FUNCTION");
    }

    /**
     * Feature: 依大分類與子分類查詢設定功能
     * Scenario: 查詢特定租戶下 FUNCTION + DASHBOARD 之設定清單
     *   Given 指定租戶 default、category FUNCTION 與 subCategory DASHBOARD
     *   When 呼叫 GetSettingUseCase 的 getByCategoryAndSubCategory 方法
     *   Then 回傳符合兩層維度的設定清單
     */
    @Test
    @DisplayName("依大分類與子分類查詢設定清單")
    void shouldGetSettingsByCategoryAndSubCategorySuccessfully() {
        // Given
        when(settingRepositoryPort.findByCategoryAndSubCategory("default", "FUNCTION", "DASHBOARD"))
                .thenReturn(List.of(sampleResult));

        // When
        List<SettingGottenResult> results = settingApplicationService.getByCategoryAndSubCategory("default", "FUNCTION", "DASHBOARD");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("DASHBOARD", results.get(0).subCategory());
        verify(settingRepositoryPort, times(1)).findByCategoryAndSubCategory("default", "FUNCTION", "DASHBOARD");
    }
}
