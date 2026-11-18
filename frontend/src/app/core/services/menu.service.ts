import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { ActiveView } from './navigation.service';

/**
 * 後端選單 API 回傳資料傳輸模型 (TenantMenuItemGottenResource)
 */
export interface TenantMenuItemGottenResource {
  id: number;
  tenant: string;
  group: string;
  code: string;
  name: string;
  description?: string;
  value: string; // JSON: {"icon":"...","badge":"...","view":"...","url":"..."}
  priorityNo: number;
  activeFlag: string;
}

/**
 * 前端選單項目模型 (MenuItem)
 */
export interface MenuItem {
  id?: number;
  code?: string;
  label: string;
  icon: string;
  active?: boolean;
  badge?: string;
  category?: string;
  view?: ActiveView;
  url?: string;
  priorityNo?: number;
}

/**
 * 前端選單群組模型 (MenuGroup)
 */
export interface MenuGroup {
  group: string;
  items: MenuItem[];
}

/**
 * 側邊欄動態選單管理服務 (MenuService)
 * <p>
 * 遵循 Function Configurable 架構，動態檢索 category=MENU 之配置，
 * 依照 subCategory 進行二級分組並按 priorityNo 排序呈現。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private readonly baseUrl = '/api/v1/settings/menus';

  private menuGroupsSubject = new BehaviorSubject<MenuGroup[]>([]);
  public menuGroups$: Observable<MenuGroup[]> = this.menuGroupsSubject.asObservable();

  // 基準備援選單 (Fallback 防止無網路或首繪閃爍)
  private readonly defaultMenuGroups: MenuGroup[] = [
    {
      group: '核心業務配置',
      items: [
        { label: '功能設定管理', icon: 'pi pi-sliders-h', active: true, badge: '核心', view: 'setting' as ActiveView, priorityNo: 1 },
        { label: '元資料字典', icon: 'pi pi-database', active: false, badge: '元資料', view: 'category' as ActiveView, priorityNo: 2 },
        { label: '動態表單設計', icon: 'pi pi-pencil', active: false, badge: '設計', view: 'dynamic-designer' as ActiveView, priorityNo: 3 },
        { label: '租戶畫像資料', icon: 'pi pi-building', active: false, view: 'tenant-profile' as ActiveView, priorityNo: 4 }
      ]
    },
    {
      group: '系統與安全維運',
      items: [
        { label: '系統權限角色', icon: 'pi pi-shield', active: false, priorityNo: 1 },
        { label: '操作稽核日誌', icon: 'pi pi-file-edit', active: false, priorityNo: 2 },
        { label: 'API 規格文件', icon: 'pi pi-book', active: false, url: '/swagger-ui.html', priorityNo: 3 }
      ]
    }
  ];

  constructor(private http: HttpClient) {
    this.menuGroupsSubject.next(this.defaultMenuGroups);
  }

  /**
   * 取得指定租戶已啟用之側邊欄選單並自動依群組 (subCategory) 聚合
   */
  public getTenantMenus(tenant?: string): Observable<MenuGroup[]> {
    let params = new HttpParams();
    if (tenant && tenant.trim()) {
      params = params.set('tenant', tenant.trim());
    }

    return this.http.get<TenantMenuItemGottenResource[]>(this.baseUrl, { params }).pipe(
      map((items) => this.transformToMenuGroups(items)),
      tap((groups) => {
        if (groups && groups.length > 0) {
          this.menuGroupsSubject.next(groups);
        }
      })
    );
  }

  /**
   * 刷新租戶選單配置
   */
  public refreshTenantMenus(tenant?: string): Observable<MenuGroup[]> {
    return this.getTenantMenus(tenant);
  }

  /**
   * 批次更新側邊欄選單順序號並即時刷新選單狀態
   *
   * @param items 欲更新順序之項目清單 (包含 id 與 optional priorityNo)
   */
  public reorderMenus(items: { id: number; priorityNo?: number }[]): Observable<MenuGroup[]> {
    return this.http.put<TenantMenuItemGottenResource[]>(`${this.baseUrl}/sort`, items).pipe(
      map((menuItems) => this.transformToMenuGroups(menuItems)),
      tap((groups) => {
        if (groups && groups.length > 0) {
          this.menuGroupsSubject.next(groups);
        }
      })
    );
  }

  /**
   * 將平坦之 TenantMenuItemGottenResource 清單轉換為分組 MenuGroup 結構
   */
  private transformToMenuGroups(items: TenantMenuItemGottenResource[]): MenuGroup[] {
    if (!items || items.length === 0) {
      return this.defaultMenuGroups;
    }

    const groupMap = new Map<string, MenuItem[]>();

    for (const item of items) {
      let icon = 'pi pi-folder';
      let badge: string | undefined;
      let view: ActiveView | undefined;
      let url: string | undefined;

      if (item.value) {
        try {
          const parsed = JSON.parse(item.value);
          if (parsed.icon) icon = parsed.icon;
          if (parsed.badge) badge = parsed.badge;
          if (parsed.view) view = parsed.view;
          if (parsed.url) url = parsed.url;
        } catch {
          if (item.value.startsWith('pi ')) {
            icon = item.value;
          }
        }
      }

      const menuItem: MenuItem = {
        id: item.id,
        code: item.code,
        label: item.name,
        icon,
        badge,
        view,
        url,
        priorityNo: item.priorityNo
      };

      const groupName = item.group || '一般功能';
      if (!groupMap.has(groupName)) {
        groupMap.set(groupName, []);
      }
      groupMap.get(groupName)!.push(menuItem);
    }

    const result: MenuGroup[] = [];
    for (const [group, menuItems] of groupMap.entries()) {
      menuItems.sort((a, b) => (a.priorityNo || 0) - (b.priorityNo || 0));
      result.push({ group, items: menuItems });
    }

    return result;
  }
}
