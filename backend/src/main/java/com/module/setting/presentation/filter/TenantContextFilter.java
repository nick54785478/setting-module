package com.module.setting.presentation.filter;

import com.module.setting.application.shared.context.TenantContextHolder;
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
 * 租戶標頭過濾器 (TenantContextFilter)
 * <p>
 * 表現層 Inbound Filter，於 HTTP 請求進入時解析 Request Header (優先讀取 "X-Tenant-Id", 其次 "X-Tenant" 或 "Tenant")，
 * 並將租戶識別碼注入應用層的 {@link TenantContextHolder} 中，供下層服務與基礎設施層取用；
 * 請求結束時於 finally 區塊徹底清除，防止執行緒污染。
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class TenantContextFilter extends OncePerRequestFilter {

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tenant = resolveTenantHeader(request);

        try {
            if (tenant != null && !tenant.isBlank()) {
                TenantContextHolder.setTenant(tenant.trim());
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
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
}
