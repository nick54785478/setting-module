package com.module.setting.application.service;

import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 分頁查詢設定用例單元測試 (GetSettingPagedUseCaseTest)
 */
@ExtendWith(MockitoExtension.class)
class GetSettingPagedUseCaseTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingApplicationService settingApplicationService;

    private SettingGottenResult sampleResult;
    private PagedDataGottenResult<SettingGottenResult> samplePagedResult;

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

        samplePagedResult = new PagedDataGottenResult<>(
                List.of(sampleResult),
                1L,
                1,
                0,
                10
        );
    }

    /**
     * Feature: 分頁查詢設定功能
     * Scenario: 依多條件動態分頁查詢成功，回傳包含設定清單的分頁封裝結果
     *   Given 提供合法之 GetSettingPagedQuery (包含 tenant, category, page, size 等)
     *   When 呼叫 GetSettingPagedUseCase 的 getPaged 方法
     *   Then 委派 SettingRepositoryPort 查詢並回傳對應的 PagedDataGottenResult
     */
    @Test
    @DisplayName("成功執行多條件動態分頁查詢")
    void shouldGetPagedSettingsSuccessfully() {
        // Given
        GetSettingPagedQuery query = new GetSettingPagedQuery(
                "default",
                "FUNCTION",
                "DASHBOARD",
                null,
                "儀錶",
                "Y",
                0,
                10
        );
        when(settingRepositoryPort.findPaged(any(GetSettingPagedQuery.class))).thenReturn(samplePagedResult);

        // When
        PagedDataGottenResult<SettingGottenResult> result = settingApplicationService.getPaged(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(0, result.page());
        assertEquals(10, result.size());

        SettingGottenResult item = result.content().get(0);
        assertEquals("default", item.tenant());
        assertEquals("FUNCTION", item.category());
        assertEquals("DASHBOARD", item.subCategory());
        assertEquals("儀錶板", item.name());

        verify(settingRepositoryPort, times(1)).findPaged(query);
    }

    /**
     * Feature: 分頁查詢設定功能
     * Scenario: 查詢無符合條件的資料時回傳空分頁結果
     *   Given 提供無匹配資料的 GetSettingPagedQuery
     *   When 呼叫 GetSettingPagedUseCase 的 getPaged 方法
     *   Then 回傳 content 為空的 PagedDataGottenResult，totalElements 為 0
     */
    @Test
    @DisplayName("查詢無匹配資料時回傳空分頁結果")
    void shouldReturnEmptyPagedResultWhenNoMatch() {
        // Given
        GetSettingPagedQuery emptyQuery = new GetSettingPagedQuery(
                "non-existent-tenant",
                null,
                null,
                null,
                null,
                "Y",
                0,
                10
        );
        PagedDataGottenResult<SettingGottenResult> emptyPagedResult = new PagedDataGottenResult<>(
                Collections.emptyList(), 0L, 0, 0, 10);
        when(settingRepositoryPort.findPaged(emptyQuery)).thenReturn(emptyPagedResult);

        // When
        PagedDataGottenResult<SettingGottenResult> result = settingApplicationService.getPaged(emptyQuery);

        // Then
        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
        assertEquals(0, result.totalPages());

        verify(settingRepositoryPort, times(1)).findPaged(emptyQuery);
    }
}
