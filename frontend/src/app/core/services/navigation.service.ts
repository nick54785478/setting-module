import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type ActiveView = 'setting' | 'category' | 'dynamic-page' | 'dynamic-designer' | 'tenant-profile' | 'locale';

/**
 * 前端全域視圖導航服務 (NavigationService)
 * <p>
 * 管理 Sidebar 與 主畫面/Header 之間的面版切換狀態。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  private currentViewSubject = new BehaviorSubject<ActiveView>('setting');
  public currentView$: Observable<ActiveView> = this.currentViewSubject.asObservable();

  public getCurrentView(): ActiveView {
    return this.currentViewSubject.value;
  }

  public setView(view: ActiveView): void {
    this.currentViewSubject.next(view);
  }
}
