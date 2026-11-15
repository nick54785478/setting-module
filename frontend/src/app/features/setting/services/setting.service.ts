import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PagedResult } from '../../../core/models/page.model';
import {
  CreateSettingRequest,
  SettingItem,
  SettingOption,
  SettingQueryParams,
  UpdateSettingRequest
} from '../models/setting.model';

/**
 * 設定領域專屬服務 (SettingService)
 * <p>
 * 遵循 Features 層規範，封裝 Setting 業務領域所有 HTTP API 調用。
 * X-Tenant-Id 標頭由 Core 層之 tenantInterceptor 自動攔截注入。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class SettingService {
  private readonly baseUrl = '/api/v1/settings';

  constructor(private http: HttpClient) {}

  /**
   * 多條件動態分頁查詢 (Specification)
   */
  public getSummary(params: SettingQueryParams): Observable<PagedResult<SettingItem>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());

    if (params.tenant && params.tenant.trim() !== '') {
      httpParams = httpParams.set('tenant', params.tenant.trim());
    }
    if (params.category && params.category.trim() !== '') {
      httpParams = httpParams.set('category', params.category.trim());
    }
    if (params.subCategory && params.subCategory.trim() !== '') {
      httpParams = httpParams.set('subCategory', params.subCategory.trim());
    }
    if (params.code && params.code.trim() !== '') {
      httpParams = httpParams.set('code', params.code.trim());
    }
    if (params.name && params.name.trim() !== '') {
      httpParams = httpParams.set('name', params.name.trim());
    }
    if (params.activeFlag && params.activeFlag.trim() !== '') {
      httpParams = httpParams.set('activeFlag', params.activeFlag.trim());
    }

    return this.http.get<PagedResult<SettingItem>>(`${this.baseUrl}/summary`, {
      params: httpParams
    });
  }

  /**
   * 取得大分類 (Category) 下拉選項清單 (META 驅動)
   */
  public getCategories(tenant?: string): Observable<SettingOption[]> {
    let httpParams = new HttpParams();
    if (tenant && tenant.trim() !== '') {
      httpParams = httpParams.set('tenant', tenant.trim());
    }
    return this.http.get<SettingOption[]>(`${this.baseUrl}/categories`, {
      params: httpParams
    });
  }

  /**
   * 依據 Category 取得子分類 (SubCategory) 下拉選項清單
   */
  public getSubCategories(category: string, tenant?: string): Observable<SettingOption[]> {
    let httpParams = new HttpParams();
    if (tenant && tenant.trim() !== '') {
      httpParams = httpParams.set('tenant', tenant.trim());
    }
    return this.http.get<SettingOption[]>(
      `${this.baseUrl}/categories/${encodeURIComponent(category)}/sub-categories`,
      { params: httpParams }
    );
  }

  /**
   * 依據主鍵 ID 查詢單一設定詳情
   */
  public getById(id: number): Observable<SettingItem> {
    return this.http.get<SettingItem>(`${this.baseUrl}/${id}`);
  }

  /**
   * 建立一筆新設定
   */
  public create(request: CreateSettingRequest): Observable<SettingItem> {
    return this.http.post<SettingItem>(this.baseUrl, request);
  }

  /**
   * 更新既有設定
   */
  public update(id: number, request: UpdateSettingRequest): Observable<SettingItem> {
    return this.http.put<SettingItem>(`${this.baseUrl}/${id}`, request);
  }

  /**
   * 邏輯刪除設定
   */
  public delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
