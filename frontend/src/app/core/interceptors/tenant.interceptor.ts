import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TenantService } from '../services/tenant.service';

/**
 * 全域多租戶 HTTP 請求標頭攔截器 (tenantInterceptor)
 * <p>
 * 遵循 Core 層規範，自動為所有後端 API 請求注入 X-Tenant-Id 標頭。
 * </p>
 */
export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const tenantService = inject(TenantService);
  const currentTenant = tenantService.getCurrentTenant();

  // 若請求未指定自訂 X-Tenant-Id，則自動填入當前租戶識別碼
  if (currentTenant && !req.headers.has('X-Tenant-Id')) {
    const clonedReq = req.clone({
      headers: req.headers.set('X-Tenant-Id', currentTenant)
    });
    return next(clonedReq);
  }

  return next(req);
};
