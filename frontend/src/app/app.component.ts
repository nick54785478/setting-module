import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { LayoutComponent } from './core/components/layout/layout.component';
import { SettingManagementComponent } from './features/setting/components/setting-management/setting-management.component';
import { CategoryManagementComponent } from './features/category/components/category-management/category-management.component';
import { NavigationService, ActiveView } from './core/services/navigation.service';

/**
 * 根元件 (AppComponent)
 * <p>
 * 遵循前端架構規範，最上層元件作為應用程式外殼載體，
 * 載入 Core 層 App Shell (LayoutComponent) 與 Feature 主畫面。
 * 根據 NavigationService 切換「功能設定管理」與「元資料字典」視圖。
 * </p>
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    LayoutComponent,
    SettingManagementComponent,
    CategoryManagementComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  currentView: ActiveView = 'setting';
  private navSub!: Subscription;

  constructor(private navigationService: NavigationService) {}

  ngOnInit(): void {
    this.navSub = this.navigationService.currentView$.subscribe((view) => {
      this.currentView = view;
    });
  }

  ngOnDestroy(): void {
    if (this.navSub) {
      this.navSub.unsubscribe();
    }
  }
}
