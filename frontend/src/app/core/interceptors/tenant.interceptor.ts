import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TenantService } from '../services/tenant.service';
import { UserService } from '../services/user.service';

/**
 * 全域使用者與租戶上下文 HTTP 請求標頭攔截器 (tenantInterceptor)
 * <p>
 * 遵循 Core 層規範，自動為所有後端 API 請求注入：
 * <ul>
 *   <li>X-Tenant-Id: 當前租戶識別碼 (固定為 CW)</li>
 *   <li>X-Username: 當前使用者帳號 (固定為 nickgh.zhang@cw.com)</li>
 * </ul>
 * </p>
 */
export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const tenantService = inject(TenantService);
  const userService = inject(UserService);

  const currentTenant = tenantService.getCurrentTenant();
  const currentUsername = userService.getCurrentUsername();

  let headers = req.headers;

  if (currentTenant && !headers.has('X-Tenant-Id')) {
    headers = headers.set('X-Tenant-Id', currentTenant);
  }

  if (currentUsername && !headers.has('X-Username')) {
    headers = headers.set('X-Username', currentUsername);
  }

  if (headers !== req.headers) {
    const clonedReq = req.clone({ headers });
    return next(clonedReq);
  }

  return next(req);
};
