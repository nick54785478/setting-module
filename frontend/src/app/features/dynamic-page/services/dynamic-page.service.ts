import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { FormField, FormFieldOption } from '../models/form-field.model';

export interface DynamicPageSettingResource {
  id: number | null;
  tenant: string;
  category: string;
  subCategory: string;
  code: string;
  name: string;
  description: string;
  value: string;
  priorityNo: number;
  activeFlag: string;
}

@Injectable({
  providedIn: 'root'
})
export class DynamicPageService {
  private readonly baseUrl = '/api/v1/settings/pages';

  constructor(private http: HttpClient) {}

  /**
   * 取得預設表單欄位範本 (涵蓋所有元件型態)
   */
  public getDefaultSchema(): FormField[] {
    return [
      {
        name: 'tenantName',
        type: 'text',
        label: '租戶正式名稱',
        validators: ['required'],
        placeholder: '請輸入租戶企業名稱'
      },
      {
        name: 'tenantCode',
        type: 'text',
        label: '租戶識別代碼',
        validators: ['required'],
        placeholder: '例如: CW, ACME'
      },
      {
        name: 'contactEmail',
        type: 'email',
        label: '系統管理員聯絡信箱',
        validators: ['required', 'email'],
        placeholder: 'admin@example.com'
      },
      {
        name: 'maxUsers',
        type: 'number',
        label: '授權使用者上限',
        validators: ['required'],
        placeholder: '例如: 100'
      },
      {
        name: 'tier',
        type: 'dropdown',
        label: '租戶合約等級方案',
        validators: ['required'],
        options: [
          { label: '標準版 (Standard)', value: 'STANDARD' },
          { label: '企業版 (Enterprise)', value: 'ENTERPRISE' },
          { label: '旗艦版 (Ultimate)', value: 'ULTIMATE' }
        ],
        placeholder: '請選擇等級方案'
      },
      {
        name: 'contractExpiry',
        type: 'calendar',
        label: '授權合約到期日',
        validators: ['required'],
        pattern: 'yy-mm-dd',
        placeholder: 'yy-mm-dd'
      },
      {
        name: 'dataRegion',
        type: 'autocomplete',
        label: '數據隔離託管區域',
        validators: [],
        placeholder: '搜尋或輸入資料中心區域 (如: 台北, 東京, 美西)',
        uri: 'regions'
      },
      {
        name: 'remarks',
        type: 'textarea',
        label: '租戶專屬自訂備註與客製說明',
        validators: [],
        rows: 3,
        cols: 30,
        placeholder: '請輸入此租戶額外自訂要求或環境隔離備註...'
      }
    ];
  }

  /**
   * 取得指定租戶之動態頁面欄位定義 (Schema)
   *
   * @param pageCode 頁面識別代碼 (預設 TENANT_PROFILE)
   * @param tenant 租戶代碼
   */
  public getPageSchema(pageCode: string = 'TENANT_PROFILE', tenant?: string): Observable<FormField[]> {
    let params = new HttpParams();
    if (tenant) {
      params = params.set('tenant', tenant);
    }

    return this.http.get<DynamicPageSettingResource>(`${this.baseUrl}/${pageCode}/schema`, { params }).pipe(
      map((res) => {
        if (!res || !res.value || res.value === '[]' || res.value === '{}') {
          return this.getDefaultSchema();
        }
        try {
          const parsed = JSON.parse(res.value);
          if (Array.isArray(parsed) && parsed.length > 0) {
            return parsed;
          }
          return this.getDefaultSchema();
        } catch {
          return this.getDefaultSchema();
        }
      })
    );
  }

  /**
   * 儲存指定租戶之動態頁面欄位定義 (Schema)
   *
   * @param pageCode 頁面識別代碼
   * @param fields 欄位結構定義陣列
   * @param tenant 租戶代碼
   */
  public savePageSchema(pageCode: string, fields: FormField[], tenant?: string): Observable<DynamicPageSettingResource> {
    let params = new HttpParams();
    if (tenant) {
      params = params.set('tenant', tenant);
    }

    const payload = JSON.stringify(fields, null, 2);
    return this.http.put<DynamicPageSettingResource>(`${this.baseUrl}/${pageCode}/schema`, payload, {
      params,
      headers: { 'Content-Type': 'application/json' }
    });
  }

  /**
   * 取得指定租戶之動態頁面填寫資料 (JSON 聚合載體)
   *
   * @param pageCode 頁面識別代碼
   * @param tenant 租戶代碼
   */
  public getPageData(pageCode: string, tenant?: string): Observable<any> {
    let params = new HttpParams();
    if (tenant) {
      params = params.set('tenant', tenant);
    }

    return this.http.get<DynamicPageSettingResource>(`${this.baseUrl}/${pageCode}`, { params }).pipe(
      map((res) => {
        if (!res || !res.value) {
          return {};
        }
        try {
          return JSON.parse(res.value);
        } catch {
          return {};
        }
      })
    );
  }

  /**
   * 儲存指定租戶之動態頁面填寫資料
   *
   * @param pageCode 頁面識別代碼
   * @param data 表單填寫資料物件
   * @param tenant 租戶代碼
   */
  public savePageData(pageCode: string, data: any, tenant?: string): Observable<DynamicPageSettingResource> {
    let params = new HttpParams();
    if (tenant) {
      params = params.set('tenant', tenant);
    }

    const payload = JSON.stringify(data);
    return this.http.put<DynamicPageSettingResource>(`${this.baseUrl}/${pageCode}`, payload, {
      params,
      headers: { 'Content-Type': 'application/json' }
    });
  }

  /**
   * 取得指定租戶之動態頁面填寫資料清單 (支援多筆資料查詢)
   *
   * @param pageCode 頁面識別代碼
   * @param tenant 租戶代碼
   */
  public getPageRecords(pageCode: string, tenant?: string): Observable<any[]> {
    return this.getPageData(pageCode, tenant).pipe(
      map((res) => {
        if (Array.isArray(res)) {
          return res;
        }
        if (res && Array.isArray(res.records)) {
          return res.records;
        }
        if (res && Object.keys(res).length > 0) {
          return [res];
        }
        // 預設基準資料
        const currentTenant = tenant || 'CW';
        return [
          {
            tenantCode: currentTenant,
            tenantName: currentTenant === 'CW' ? '國泰世華銀行總行' : `客製租戶空間 (${currentTenant})`,
            contactEmail: 'admin@cw.com',
            maxUsers: 500,
            tier: 'ENTERPRISE',
            contractExpiry: '2027-12-31',
            dataRegion: 'TW-TPE-1',
            remarks: '核心商業租戶，支援高可用性與多可用區數據隔離。'
          }
        ];
      })
    );
  }

  /**
   * 儲存指定租戶之動態頁面填寫資料清單
   *
   * @param pageCode 頁面識別代碼
   * @param records 資料紀錄陣列
   * @param tenant 租戶代碼
   */
  public savePageRecords(pageCode: string, records: any[], tenant?: string): Observable<DynamicPageSettingResource> {
    return this.savePageData(pageCode, records, tenant);
  }

  /**
   * AutoComplete 本地/遠端建議資料查詢
   *
   * @param query 使用者輸入查詢字串
   * @param uri 查詢路徑/標識
   */
  public searchAutoComplete(query: string, uri: string): Observable<FormFieldOption[]> {
    const regionOptions: FormFieldOption[] = [
      { label: '台灣台北 (ap-east-1)', value: 'TW-TPE-1' },
      { label: '日本東京 (ap-northeast-1)', value: 'JP-TYO-1' },
      { label: '美國西岸 (us-west-2)', value: 'US-PDX-1' },
      { label: '德國法蘭克福 (eu-central-1)', value: 'EU-FRA-1' },
      { label: '新加坡 (ap-southeast-1)', value: 'SG-SIN-1' }
    ];

    if (!query) {
      return of(regionOptions);
    }

    const lower = query.toLowerCase();
    const filtered = regionOptions.filter(
      (opt) => opt.label.toLowerCase().includes(lower) || String(opt.value).toLowerCase().includes(lower)
    );
    return of(filtered);
  }
}
