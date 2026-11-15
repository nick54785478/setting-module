import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { TenantOption } from '../models/tenant.model';

/**
 * 全域單例多租戶管理服務 (TenantService)
 * <p>
 * 遵循 Core 層規範，維護全系統當前租戶識別碼狀態並提供響應式廣播。
 * 現行版本依需求固定寫死為 'CW' 租戶，未來擴充動態切換時由 Backlog (BL-001) 實作接管。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService {
  /**
   * 當前系統固定租戶識別碼 (CW)
   */
  public static readonly DEFAULT_TENANT = 'CW';

  private tenantSubject = new BehaviorSubject<string>(TenantService.DEFAULT_TENANT);
  public tenant$: Observable<string> = this.tenantSubject.asObservable();

  public readonly defaultTenantOptions: TenantOption[] = [
    { label: 'CW 租戶 (CW)', value: 'CW' }
  ];

  /**
   * 取得當前租戶代碼 (固定回傳 CW)
   */
  public getCurrentTenant(): string {
    return TenantService.DEFAULT_TENANT;
  }

  /**
   * 切換租戶代碼
   * 現階段鎖定為 CW，保留方法合約供未來擴充
   *
   * @param tenant 租戶識別碼
   */
  public setTenant(tenant: string): void {
    // 依現行架構需求，前端租戶固定鎖定為 CW
    this.tenantSubject.next(TenantService.DEFAULT_TENANT);
  }
}
