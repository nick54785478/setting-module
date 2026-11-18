import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { TooltipModule } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { NavigationService, ActiveView } from '../../services/navigation.service';
import { TenantService } from '../../services/tenant.service';
import { MenuService, MenuGroup, MenuItem } from '../../services/menu.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, TooltipModule, ToastModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() collapsed: boolean = false;
  @Input() mobileOpen: boolean = false;
  @Output() toggleCollapse = new EventEmitter<void>();
  @Output() closeMobile = new EventEmitter<void>();

  private navSub!: Subscription;
  private tenantSub!: Subscription;
  private menuSub!: Subscription;

  currentView: ActiveView = 'setting';
  menuGroups: MenuGroup[] = [];

  // 拖拉排序模式狀態
  isReorderMode: boolean = false;
  draggedGroup: MenuGroup | null = null;
  draggedItem: MenuItem | null = null;
  dragOverItem: MenuItem | null = null;

  constructor(
    private navigationService: NavigationService,
    private tenantService: TenantService,
    private menuService: MenuService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    // 訂閱當前功能視圖
    this.navSub = this.navigationService.currentView$.subscribe((view) => {
      this.currentView = view;
      this.syncActiveState(view);
    });

    // 訂閱動態選單資料
    this.menuSub = this.menuService.menuGroups$.subscribe((groups) => {
      this.menuGroups = groups;
      this.syncActiveState(this.currentView);
    });

    // 租戶切換時動態重新拉取選單配置 (Function Configurable)
    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.menuService.getTenantMenus(tenant).subscribe();
    });

    // 初始載入當前租戶選單
    const initialTenant = this.tenantService.getCurrentTenant();
    this.menuService.getTenantMenus(initialTenant).subscribe();
  }

  ngOnDestroy(): void {
    if (this.navSub) this.navSub.unsubscribe();
    if (this.tenantSub) this.tenantSub.unsubscribe();
    if (this.menuSub) this.menuSub.unsubscribe();
  }

  private syncActiveState(view: ActiveView): void {
    if (!this.menuGroups) return;
    this.menuGroups.forEach((g) => {
      g.items.forEach((i) => {
        if (i.view) {
          i.active = i.view === view || ((view === 'dynamic-page' || view === 'tenant-profile') && (i.view === 'tenant-profile' || i.view === 'dynamic-page'));
        } else if (i.code === 'MENU_TENANT' && (view === 'tenant-profile' || view === 'dynamic-page')) {
          i.active = true;
        } else {
          i.active = false;
        }
      });
    });
  }

  onMenuClick(item: MenuItem): void {
    if (this.isReorderMode) {
      // 拖拉排序模式中停用點擊跳轉，避免拖曳時誤觸發導航
      return;
    }

    if (item.url) {
      window.open(item.url, '_blank');
      this.closeMobile.emit();
      return;
    }

    if (item.view) {
      this.navigationService.setView(item.view);
    } else if (item.code === 'MENU_TENANT') {
      this.navigationService.setView('tenant-profile');
    } else if (item.code === 'MENU_DESIGNER') {
      this.navigationService.setView('dynamic-designer');
    } else if (item.code === 'MENU_SETTING') {
      this.navigationService.setView('setting');
    } else if (item.code === 'MENU_CATEGORY') {
      this.navigationService.setView('category');
    } else {
      this.menuGroups.forEach((g) => g.items.forEach((i) => (i.active = false)));
      item.active = true;
    }

    this.closeMobile.emit();
  }

  /**
   * 切換選單拖拉排序模式
   */
  toggleReorderMode(): void {
    this.isReorderMode = !this.isReorderMode;
    if (this.isReorderMode && this.collapsed) {
      // 若側邊欄為縮合狀態，切換排序時自動展開便於檢視與拖曳操作
      this.toggleCollapse.emit();
    }
    if (!this.isReorderMode) {
      this.draggedGroup = null;
      this.draggedItem = null;
      this.dragOverItem = null;
    }
  }

  /**
   * 拖曳開始
   */
  onDragStart(group: MenuGroup, item: MenuItem, event: DragEvent): void {
    if (!this.isReorderMode) return;
    this.draggedGroup = group;
    this.draggedItem = item;
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/plain', item.code || '');
    }
  }

  /**
   * 拖曳經過
   */
  onDragOver(event: DragEvent): void {
    if (!this.isReorderMode) return;
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  /**
   * 拖曳進入目標項目
   */
  onDragEnter(item: MenuItem): void {
    if (!this.isReorderMode) return;
    if (this.draggedItem && this.draggedItem !== item) {
      this.dragOverItem = item;
    }
  }

  /**
   * 拖曳離開目標項目
   */
  onDragLeave(item: MenuItem): void {
    if (!this.isReorderMode) return;
    if (this.dragOverItem === item) {
      this.dragOverItem = null;
    }
  }

  /**
   * 放置目標項目 (觸發重排與即時 API 儲存)
   */
  onDrop(targetGroup: MenuGroup, targetItem: MenuItem, event: DragEvent): void {
    if (!this.isReorderMode || !this.draggedItem || !this.draggedGroup) return;
    event.preventDefault();

    // 限制於同一個群組內進行前後順序排列
    if (this.draggedGroup === targetGroup && this.draggedItem !== targetItem) {
      const items = targetGroup.items;
      const fromIndex = items.indexOf(this.draggedItem);
      const toIndex = items.indexOf(targetItem);

      if (fromIndex !== -1 && toIndex !== -1) {
        // 1. 本地陣列即時調換位置
        items.splice(fromIndex, 1);
        items.splice(toIndex, 0, this.draggedItem);

        // 2. 重新指派 priorityNo (1, 2, 3...)
        const reorderPayload = items.map((it, idx) => {
          it.priorityNo = idx + 1;
          return {
            id: it.id!,
            priorityNo: idx + 1
          };
        });

        // 3. 呼叫後端選單專用排序 API 永久保存
        this.menuService.reorderMenus(reorderPayload).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: '排序已儲存',
              detail: `已成功更新「${targetGroup.group}」的選單排列順序`,
              life: 2500
            });
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: '排序儲存失敗',
              detail: err?.error?.message || err?.message || '伺服器儲存選單順序失敗',
              life: 4000
            });
            // 若失敗則重新拉取還原
            const tenant = this.tenantService.getCurrentTenant();
            this.menuService.getTenantMenus(tenant).subscribe();
          }
        });
      }
    }

    this.draggedGroup = null;
    this.draggedItem = null;
    this.dragOverItem = null;
  }

  /**
   * 拖曳結束復原狀態
   */
  onDragEnd(): void {
    this.draggedGroup = null;
    this.draggedItem = null;
    this.dragOverItem = null;
  }
}
