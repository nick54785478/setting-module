import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

/**
 * 租戶語系項目資料模型 (TenantLocaleItem)
 */
export interface TenantLocaleItem {
  id: number;
  tenant: string;
  code: string;
  name: string;
  description?: string;
  isDefault: boolean;
  priorityNo: number;
  activeFlag: 'Y' | 'N';
}

/**
 * 新增語系配置請求模型
 */
export interface CreateTenantLocaleRequest {
  code: string;
  name: string;
  description?: string;
  isDefault: boolean;
  priorityNo: number;
}

/**
 * 更新語系配置請求模型
 */
export interface UpdateTenantLocaleRequest {
  name: string;
  description?: string;
  isDefault: boolean;
  priorityNo: number;
  activeFlag: string;
}

/**
 * 系統租戶語系管理服務 (LocaleService)
 * <p>
 * 遵循 Core 層全域服務規範，管理當前選用語系、即時切換與向後端同步各租戶之 LOCALE 配置。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class LocaleService {
  private readonly baseUrl = '/api/v1/settings';
  private readonly STORAGE_KEY = 'app_locale';

  // 當前選取之系統語系代碼 (例如: zh-TW, en-US)
  private currentLocaleSubject: BehaviorSubject<string>;
  public currentLocale$: Observable<string>;

  // 當前租戶可用之語系列表快取
  private localesSubject = new BehaviorSubject<TenantLocaleItem[]>([]);
  public locales$: Observable<TenantLocaleItem[]> = this.localesSubject.asObservable();

  constructor(private http: HttpClient) {
    const savedLocale = localStorage.getItem(this.STORAGE_KEY) || 'zh-TW';
    this.currentLocaleSubject = new BehaviorSubject<string>(savedLocale);
    this.currentLocale$ = this.currentLocaleSubject.asObservable();
  }

  /**
   * 取得當前選定之語系代碼
   */
  public getCurrentLocale(): string {
    return this.currentLocaleSubject.value;
  }

  /**
   * 切換並記憶系統當前語系
   */
  public setLocale(localeCode: string): void {
    if (!localeCode) return;
    localStorage.setItem(this.STORAGE_KEY, localeCode);
    this.currentLocaleSubject.next(localeCode);
  }

  /**
   * 取得指定租戶已啟用之系統語系列表 (供 Header 選單使用)
   */
  public getTenantLocales(tenant?: string): Observable<TenantLocaleItem[]> {
    let params = new HttpParams();
    if (tenant && tenant.trim()) {
      params = params.set('tenant', tenant.trim());
    }
    return this.http.get<TenantLocaleItem[]>(`${this.baseUrl}/locales`, { params }).pipe(
      tap((locales) => {
        this.localesSubject.next(locales || []);
        // 若當前記憶語系不在有效清單中，自動回退預設語系
        if (locales && locales.length > 0) {
          const current = this.getCurrentLocale();
          const exists = locales.some((l) => l.code.toLowerCase() === current.toLowerCase());
          if (!exists) {
            const defaultLoc = locales.find((l) => l.isDefault) || locales[0];
            this.setLocale(defaultLoc.code);
          }
        }
      })
    );
  }

  /**
   * 刷新指定租戶之系統語系列表 (主動呼叫 API 重新檢索並推播)
   */
  public refreshTenantLocales(tenant?: string): Observable<TenantLocaleItem[]> {
    return this.getTenantLocales(tenant);
  }

  /**
   * 取得指定租戶之所有語系列表 (含停用，供維護彈窗使用)
   */
  public getAllTenantLocales(tenant?: string): Observable<TenantLocaleItem[]> {
    let params = new HttpParams();
    if (tenant && tenant.trim()) {
      params = params.set('tenant', tenant.trim());
    }
    return this.http.get<TenantLocaleItem[]>(`${this.baseUrl}/locales/all`, { params });
  }

  /**
   * 為指定租戶新增一筆語系設定 (完成後自動刷新語系清單 API)
   */
  public createLocale(tenant: string, request: CreateTenantLocaleRequest): Observable<any> {
    const payload = {
      tenant: tenant || 'CW',
      category: 'LOCALE',
      subCategory: 'UI',
      code: request.code.trim(),
      name: request.name.trim(),
      description: request.description ? request.description.trim() : '',
      value: request.isDefault ? 'DEFAULT' : '',
      priorityNo: request.priorityNo || 1
    };
    return this.http.post(this.baseUrl, payload).pipe(
      tap(() => this.getTenantLocales(tenant).subscribe())
    );
  }

  /**
   * 更新指定語系設定 (完成後自動刷新語系清單 API)
   */
  public updateLocale(id: number, request: UpdateTenantLocaleRequest, tenant?: string): Observable<any> {
    const payload = {
      category: 'LOCALE',
      subCategory: 'UI',
      name: request.name.trim(),
      description: request.description ? request.description.trim() : '',
      value: request.isDefault ? 'DEFAULT' : '',
      priorityNo: request.priorityNo || 1,
      activeFlag: request.activeFlag || 'Y'
    };
    return this.http.put(`${this.baseUrl}/${id}`, payload).pipe(
      tap(() => this.getTenantLocales(tenant).subscribe())
    );
  }

  /**
   * 設定指定語系為該租戶之預設語系 (完成後自動刷新語系清單 API)
   */
  public setLocaleDefault(id: number, tenant?: string): Observable<TenantLocaleItem> {
    return this.http.put<TenantLocaleItem>(`${this.baseUrl}/locales/${id}/default`, {}).pipe(
      tap(() => this.getTenantLocales(tenant).subscribe())
    );
  }

  /**
   * 刪除指定語系設定 (完成後自動刷新語系清單 API)
   */
  public deleteLocale(id: number, tenant?: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => this.getTenantLocales(tenant).subscribe())
    );
  }
}
