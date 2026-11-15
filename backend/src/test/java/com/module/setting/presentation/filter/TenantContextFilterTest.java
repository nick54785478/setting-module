package com.module.setting.presentation.filter;

import com.module.setting.application.shared.context.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 租戶上下文過濾器單元測試 (TenantContextFilterTest)
 */
@ExtendWith(MockitoExtension.class)
class TenantContextFilterTest {

    private final TenantContextFilter tenantContextFilter = new TenantContextFilter();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    /**
     * Feature: 租戶標頭攔截與上下文注入
     * Scenario: 當 HTTP RequestHeader 帶有 X-Tenant-Id 時，過濾器將租戶注入 ContextHolder 且執行完畢後徹底清除
     *   Given RequestHeader 包含 "X-Tenant-Id: tenant-alpha"
     *   When Filter 攔截並執行 doFilterInternal
     *   Then 在 FilterChain 執行期間 ContextHolder 可取用 "tenant-alpha"，且完成後 ContextHolder 自動被 clear 清空
     */
    @Test
    @DisplayName("RequestHeader 包含 X-Tenant-Id 時正確設定與清除 TenantContextHolder")
    void shouldSetAndClearTenantContextFromHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader(TenantContextFilter.HEADER_X_TENANT_ID)).thenReturn("tenant-alpha");

        doAnswer(invocation -> {
            // 驗證在 filterChain 內部可取得注入的 Tenant
            assertEquals("tenant-alpha", TenantContextHolder.getTenant());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        tenantContextFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        // 驗證 finally 區塊已清空 ThreadLocal
        assertNull(TenantContextHolder.getTenant());
    }

    /**
     * Feature: 租戶標頭攔截與上下文注入
     * Scenario: 當 HTTP 請求未攜帶任何租戶標頭時，Filter 不設定 ContextHolder
     *   Given RequestHeader 未帶有任何租戶標頭
     *   When Filter 攔截並執行 doFilterInternal
     *   Then ContextHolder 始終為 null
     */
    @Test
    @DisplayName("RequestHeader 無租戶標頭時 ContextHolder 保持為 null")
    void shouldKeepNullWhenNoHeaderPresent() throws ServletException, IOException {
        // Given
        when(request.getHeader(TenantContextFilter.HEADER_X_TENANT_ID)).thenReturn(null);
        when(request.getHeader(TenantContextFilter.HEADER_X_TENANT)).thenReturn(null);
        when(request.getHeader(TenantContextFilter.HEADER_TENANT)).thenReturn(null);

        doAnswer(invocation -> {
            assertNull(TenantContextHolder.getTenant());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        tenantContextFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(TenantContextHolder.getTenant());
    }
}
