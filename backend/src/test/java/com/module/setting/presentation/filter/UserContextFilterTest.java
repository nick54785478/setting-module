package com.module.setting.presentation.filter;

import com.module.setting.application.shared.context.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 使用者與租戶上下文標頭過濾器單元測試 (UserContextFilterTest)
 */
class UserContextFilterTest {

    private UserContextFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new UserContextFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    /**
     * Feature: 使用者與租戶上下文解析
     * Scenario: RequestHeader 包含 X-Tenant-Id 與 X-Username 時正確設定與清除 UserContextHolder
     * Given 提供包含 X-Tenant-Id = "tenant-alpha" 與 X-Username = "nickgh.zhang@cw.com" 的 HTTP 請求
     * When 通過 UserContextFilter 執行過濾
     * Then 在 FilterChain 執行期間可取得該 Tenant 與 Username，且請求結束後 Context 自動清除
     */
    @Test
    @DisplayName("RequestHeader 包含 X-Tenant-Id 與 X-Username 時正確設定與清除 UserContextHolder")
    void shouldSetAndClearUserContextFromHeader() throws ServletException, IOException {
        // Given
        request.addHeader(UserContextFilter.HEADER_X_TENANT_ID, "tenant-alpha");
        request.addHeader(UserContextFilter.HEADER_X_USERNAME, "nickgh.zhang@cw.com");

        doAnswer(invocation -> {
            // Then: 在 FilterChain 執行期間，ThreadLocal 應能正確取得 tenant 與 username
            assertEquals("tenant-alpha", UserContextHolder.getTenant());
            assertEquals("nickgh.zhang@cw.com", UserContextHolder.getUsername());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then: 請求結束後，ThreadLocal 必須被完整清理
        assertNull(UserContextHolder.getTenant());
        assertNull(UserContextHolder.getUsername());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Feature: 使用者與租戶上下文解析
     * Scenario: RequestHeader 未帶有租戶或使用者標頭時安全放行
     * Given 提供未包含任何 Header 的 HTTP 請求
     * When 通過 UserContextFilter 執行過濾
     * Then Context 為 null 且正常調用下一層 Filter
     */
    @Test
    @DisplayName("RequestHeader 無標頭時安全放行且 Context 為 null")
    void shouldHandleMissingHeadersGracefully() throws ServletException, IOException {
        // Given (no headers)

        doAnswer(invocation -> {
            assertNull(UserContextHolder.getTenant());
            assertNull(UserContextHolder.getUsername());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(UserContextHolder.getTenant());
        assertNull(UserContextHolder.getUsername());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
