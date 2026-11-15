import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoryItem, CreateCategoryRequest, UpdateCategoryRequest } from '../models/category.model';

/**
 * 維度元資料專屬服務 (CategoryService)
 * <p>
 * 專門負責維護 Meta Category (SYSTEM, ROLE, GROUP, FUNCTION 等業務大分類字典)。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly baseUrl = '/api/v1/categories';

  constructor(private http: HttpClient) {}

  /**
   * 取得維度元資料清單
   */
  public getCategories(tenant?: string, includeInactive: boolean = false): Observable<CategoryItem[]> {
    let params = new HttpParams().set('includeInactive', includeInactive.toString());
    if (tenant && tenant.trim() !== '') {
      params = params.set('tenant', tenant.trim());
    }
    return this.http.get<CategoryItem[]>(this.baseUrl, { params });
  }

  /**
   * 依 ID 取得單一維度
   */
  public getById(id: number): Observable<CategoryItem> {
    return this.http.get<CategoryItem>(`${this.baseUrl}/${id}`);
  }

  /**
   * 建立一筆新維度
   */
  public create(request: CreateCategoryRequest): Observable<CategoryItem> {
    return this.http.post<CategoryItem>(this.baseUrl, request);
  }

  /**
   * 更新既有維度
   */
  public update(id: number, request: UpdateCategoryRequest): Observable<CategoryItem> {
    return this.http.put<CategoryItem>(`${this.baseUrl}/${id}`, request);
  }

  /**
   * 邏輯刪除維度
   */
  public delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
