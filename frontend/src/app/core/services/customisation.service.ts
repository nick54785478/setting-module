import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

/**
 * 個人化配置前端服務 (CustomisationService)
 * <p>
 * 提供向後端查詢、儲存與還原 Table 欄位自訂顯示 (FieldView) 之 API。
 * 後端以輕量化之「逗號分隔 (CSV)」儲存欄位代碼，徹底解耦 UI 標籤文案與多語系 (i18n)。
 * 同步支援本地快取 (localStorage) 確保畫面重新整理時零閃爍即時還原。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class CustomisationService {
  private readonly baseUrl = '/api/v1/user/customisations';

  constructor(private http: HttpClient) {}

  /**
   * 取得本地快取的欄位清單 (用於瞬間初始化防閃爍)
   */
  public getLocalCustomisation(component: string, type: string = 'FieldView'): string[] | null {
    try {
      const cached = localStorage.getItem(`customisation_${component}_${type}`);
      return cached ? JSON.parse(cached) : null;
    } catch {
      return null;
    }
  }

  /**
   * 寫入本地快取
   */
  public setLocalCustomisation(component: string, fields: string[], type: string = 'FieldView'): void {
    try {
      localStorage.setItem(`customisation_${component}_${type}`, JSON.stringify(fields));
    } catch {
      // 忽略存取限制例外
    }
  }

  /**
   * 取得特定元件與種類的使用者個人化欄位清單 (向後端查詢)
   */
  public getCustomisation(component: string, type: string = 'FieldView'): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/${component}/${type}`).pipe(
      tap((fields) => {
        if (fields && fields.length > 0) {
          this.setLocalCustomisation(component, fields, type);
        }
      })
    );
  }

  /**
   * 儲存個人化欄位清單 (傳遞欄位 field 代碼陣列)
   */
  public saveCustomisation(
    component: string,
    fields: string[],
    type: string = 'FieldView'
  ): Observable<string[]> {
    this.setLocalCustomisation(component, fields, type);
    return this.http.put<string[]>(`${this.baseUrl}/${component}/${type}`, fields);
  }

  /**
   * 重設/還原個人化配置為系統預設
   */
  public resetCustomisation(component: string, type: string = 'FieldView'): Observable<void> {
    try {
      localStorage.removeItem(`customisation_${component}_${type}`);
    } catch {
      // 忽略存取限制例外
    }
    return this.http.delete<void>(`${this.baseUrl}/${component}/${type}`);
  }
}
