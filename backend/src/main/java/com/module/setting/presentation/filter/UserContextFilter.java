package com.module.setting.presentation.filter;

import com.module.setting.application.shared.context.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 使用者與租戶上下文標頭過濾器 (UserContextFilter)
 * <p>
 * 表現層 Inbound Filter，於 HTTP 請求進入時解析 Request Header：
 * <ul>
 *     <li>租戶標頭：優先讀取 "X-Tenant-Id"，其次 "X-Tenant" 或 "Tenant"</li>
 *     <li>使用者標頭：優先讀取 "X-Username"，其次 "X-User-Id" 或 "X-User"</li>
 * </ul>
 * 並將租戶識別碼與使用者名稱注入應用層的 {@link UserContextHolder} 中，供下層服務與基礎設施層取用；
 * 請求結束時於 finally 區塊徹底清除，防止執行緒池複用造成資料污染。
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class UserContextFilter extends OncePerRequestFilter {

    /**
     * 主要租戶標頭名稱
     */
    public static final String HEADER_X_TENANT_ID = "X-Tenant-Id";

    /**
     * 備用租戶標頭名稱
     */
    public static final String HEADER_X_TENANT = "X-Tenant";

    /**
     * 通用租戶標頭名稱
     */
    public static final String HEADER_TENANT = "Tenant";

    /**
     * 主要使用者名稱標頭
     */
    public static final String HEADER_X_USERNAME = "X-Username";

    /**
     * 備用使用者 ID 標頭
     */
    public static final String HEADER_X_USER_ID = "X-User-Id";

    /**
     * 通用使用者標頭
     */
    public static final String HEADER_X_USER = "X-User";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tenant = resolveTenantHeader(request);
        String username = resolveUsernameHeader(request);

        try {
            if (tenant != null && !tenant.isBlank()) {
                UserContextHolder.setTenant(tenant.trim());
            }
            if (username != null && !username.isBlank()) {
                UserContextHolder.setUsername(username.trim());
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * 解析 HTTP 請求標頭中的租戶識別碼
     *
     * @param request HTTP 請求物件
     * @return 租戶識別碼字串，若無則回傳 null
     */
    private String resolveTenantHeader(HttpServletRequest request) {
        String tenant = request.getHeader(HEADER_X_TENANT_ID);
        if (tenant == null || tenant.isBlank()) {
            tenant = request.getHeader(HEADER_X_TENANT);
        }
        if (tenant == null || tenant.isBlank()) {
            tenant = request.getHeader(HEADER_TENANT);
        }
        return tenant;
    }

    /**
     * 解析 HTTP 請求標頭中的使用者名稱
     *
     * @param request HTTP 請求物件
     * @return 使用者名稱字串，若無則回傳 null
     */
    private String resolveUsernameHeader(HttpServletRequest request) {
        String username = request.getHeader(HEADER_X_USERNAME);
        if (username == null || username.isBlank()) {
            username = request.getHeader(HEADER_X_USER_ID);
        }
        if (username == null || username.isBlank()) {
            username = request.getHeader(HEADER_X_USER);
        }
        return username;
    }
}
