import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { TooltipModule } from 'primeng/tooltip';
import { NavigationService, ActiveView } from '../../services/navigation.service';

export interface MenuItem {
  label: string;
  icon: string;
  active?: boolean;
  badge?: string;
  category?: string;
  view?: ActiveView;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, TooltipModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() collapsed: boolean = false;
  @Input() mobileOpen: boolean = false;
  @Output() toggleCollapse = new EventEmitter<void>();
  @Output() closeMobile = new EventEmitter<void>();

  private navSub!: Subscription;

  menuGroups = [
    {
      group: '核心業務配置',
      items: [
        { label: '功能設定管理', icon: 'pi pi-sliders-h', active: true, badge: '核心', view: 'setting' as ActiveView },
        { label: '元資料字典', icon: 'pi pi-database', active: false, badge: '元資料', view: 'category' as ActiveView },
        { label: '租戶隔離中心', icon: 'pi pi-building', active: false }
      ]
    },
    {
      group: '系統與安全維運',
      items: [
        { label: '系統權限角色', icon: 'pi pi-shield', active: false },
        { label: '操作稽核日誌', icon: 'pi pi-file-edit', active: false },
        { label: 'API 規格文件', icon: 'pi pi-book', active: false }
      ]
    }
  ];

  constructor(private navigationService: NavigationService) {}

  ngOnInit(): void {
    this.navSub = this.navigationService.currentView$.subscribe((view) => {
      this.syncActiveState(view);
    });
  }

  ngOnDestroy(): void {
    if (this.navSub) {
      this.navSub.unsubscribe();
    }
  }

  private syncActiveState(view: ActiveView): void {
    this.menuGroups.forEach((g) => {
      g.items.forEach((i) => {
        if (i.view) {
          i.active = i.view === view;
        }
      });
    });
  }

  onMenuClick(item: any): void {
    if (item.label === 'API 規格文件') {
      window.open('/swagger-ui.html', '_blank');
      this.closeMobile.emit();
      return;
    }

    if (item.view) {
      this.navigationService.setView(item.view);
    } else {
      this.menuGroups.forEach((g) => g.items.forEach((i) => (i.active = false)));
      item.active = true;
    }

    this.closeMobile.emit();
  }
}
