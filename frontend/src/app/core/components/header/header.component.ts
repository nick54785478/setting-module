import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { DropdownModule } from 'primeng/dropdown';
import { TooltipModule } from 'primeng/tooltip';
import { TenantService } from '../../services/tenant.service';
import { UserService } from '../../services/user.service';
import { NavigationService, ActiveView } from '../../services/navigation.service';
import { TenantOption } from '../../models/tenant.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, FormsModule, DropdownModule, TooltipModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() sidebarCollapsed: boolean = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  currentTenant: string = 'CW';
  currentUsername: string = 'nickgh.zhang@cw.com';
  tenantOptions: TenantOption[] = [];
  currentViewLabel: string = '功能設定管理';

  private navSub!: Subscription;

  constructor(
    private tenantService: TenantService,
    private userService: UserService,
    private navigationService: NavigationService
  ) {}

  ngOnInit(): void {
    this.currentTenant = this.tenantService.getCurrentTenant();
    this.currentUsername = this.userService.getCurrentUsername();
    this.tenantOptions = this.tenantService.defaultTenantOptions;

    this.navSub = this.navigationService.currentView$.subscribe((view: ActiveView) => {
      this.currentViewLabel = view === 'category' ? '元資料字典' : '功能設定管理';
    });
  }

  ngOnDestroy(): void {
    if (this.navSub) {
      this.navSub.unsubscribe();
    }
  }

  onTenantChange(): void {
    this.tenantService.setTenant(this.currentTenant);
  }
}
