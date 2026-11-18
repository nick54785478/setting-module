import { ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { DropdownModule } from 'primeng/dropdown';
import { TooltipModule } from 'primeng/tooltip';
import { OverlayPanel, OverlayPanelModule } from 'primeng/overlaypanel';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { CheckboxModule } from 'primeng/checkbox';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../services/tenant.service';
import { UserService } from '../../services/user.service';
import { NavigationService, ActiveView } from '../../services/navigation.service';
import { LocaleService, TenantLocaleItem } from '../../services/locale.service';
import { TenantOption } from '../../models/tenant.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DropdownModule,
    TooltipModule,
    OverlayPanelModule,
    DialogModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    CheckboxModule,
    ToastModule,
    ConfirmDialogModule
  ],
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

  // 語系狀態
  currentLocaleCode: string = 'zh-TW';
  currentLocaleName: string = '繁體中文';
  tenantLocales: TenantLocaleItem[] = [];

  // 維護彈窗狀態
  localeManageVisible: boolean = false;
  allTenantLocales: TenantLocaleItem[] = [];
  loadingLocales: boolean = false;

  // 維護表單
  isEditing: boolean = false;
  editId: number | null = null;
  formCode: string = '';
  formName: string = '';
  formDesc: string = '';
  formIsDefault: boolean = false;
  formPriorityNo: number = 1;
  formActiveFlag: string = 'Y';
  selectedPreset: any = null;

  presetLocales = [
    { label: '繁體中文 (zh-TW)', code: 'zh-TW', name: '繁體中文', desc: '繁體中文 (台灣)' },
    { label: '簡體中文 (zh-CN)', code: 'zh-CN', name: '简体中文', desc: '简体中文 (中国)' },
    { label: 'English (en-US)', code: 'en-US', name: 'English', desc: 'English (United States)' },
    { label: '日本語 (ja-JP)', code: 'ja-JP', name: '日本語', desc: '日本語 (日本)' },
    { label: '한국어 (ko-KR)', code: 'ko-KR', name: '한국어', desc: '한국어 (대한민국)' }
  ];

  private navSub!: Subscription;
  private tenantSub!: Subscription;
  private localeSub!: Subscription;
  private localesListSub!: Subscription;

  constructor(
    private tenantService: TenantService,
    private userService: UserService,
    private navigationService: NavigationService,
    private localeService: LocaleService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentTenant = this.tenantService.getCurrentTenant();
    this.currentUsername = this.userService.getCurrentUsername();
    this.tenantOptions = this.tenantService.defaultTenantOptions;

    this.navSub = this.navigationService.currentView$.subscribe((view: ActiveView) => {
      if (view === 'category') {
        this.currentViewLabel = '元資料字典';
      } else if (view === 'dynamic-page') {
        this.currentViewLabel = '租戶隔離中心';
      } else {
        this.currentViewLabel = '功能設定管理';
      }
    });

    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.currentTenant = tenant;
      this.loadTenantLocales();
    });

    this.localeSub = this.localeService.currentLocale$.subscribe((code) => {
      this.currentLocaleCode = code;
      this.updateCurrentLocaleDisplay();
    });

    this.localesListSub = this.localeService.locales$.subscribe((locales) => {
      this.tenantLocales = locales || [];
      this.updateCurrentLocaleDisplay();
      this.cdr.markForCheck();
    });

    this.loadTenantLocales();
  }

  ngOnDestroy(): void {
    if (this.navSub) this.navSub.unsubscribe();
    if (this.tenantSub) this.tenantSub.unsubscribe();
    if (this.localeSub) this.localeSub.unsubscribe();
    if (this.localesListSub) this.localesListSub.unsubscribe();
  }

  onTenantChange(): void {
    this.tenantService.setTenant(this.currentTenant);
    this.loadTenantLocales();
  }

  /**
   * 點擊地球按鈕時刷新並展開選單
   */
  openLocaleDropdown(op: OverlayPanel, event: MouseEvent): void {
    this.loadTenantLocales();
    op.toggle(event);
  }

  /**
   * 載入當前租戶已啟用的系統語系 (呼叫 API 重新檢索)
   */
  loadTenantLocales(): void {
    this.localeService.refreshTenantLocales(this.currentTenant).subscribe({
      next: (locales) => {
        this.tenantLocales = locales || [];
        this.updateCurrentLocaleDisplay();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load tenant locales', err);
      }
    });
  }

  private updateCurrentLocaleDisplay(): void {
    if (!this.tenantLocales || this.tenantLocales.length === 0) {
      this.currentLocaleName = '未設定語系';
      return;
    }
    const match = this.tenantLocales.find(
      (l) => l.code.toLowerCase() === this.currentLocaleCode.toLowerCase()
    );
    if (match) {
      this.currentLocaleName = match.name;
    } else {
      const defaultLoc = this.tenantLocales.find((l) => l.isDefault) || this.tenantLocales[0];
      this.currentLocaleName = defaultLoc ? defaultLoc.name : this.currentLocaleCode;
    }
  }

  /**
   * 選擇切換系統語系
   */
  selectLocale(loc: TenantLocaleItem, op: OverlayPanel): void {
    this.localeService.setLocale(loc.code);
    op.hide();
    this.messageService.add({
      severity: 'info',
      summary: '語系已切換',
      detail: `系統已切換為：${loc.name} (${loc.code})`,
      life: 2500
    });
    this.loadTenantLocales();
  }

  /**
   * 開啟租戶語系維護對話框
   */
  openLocaleManageDialog(): void {
    this.localeManageVisible = true;
    this.resetLocaleForm();
    this.loadAllTenantLocales();
  }

  /**
   * 載入當前租戶所有語系列表 (含停用)
   */
  loadAllTenantLocales(): void {
    this.loadingLocales = true;
    this.localeService.getAllTenantLocales(this.currentTenant).subscribe({
      next: (list) => {
        this.allTenantLocales = list || [];
        this.loadingLocales = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loadingLocales = false;
        console.error('Failed to load all tenant locales', err);
        this.messageService.add({
          severity: 'error',
          summary: '載入失敗',
          detail: '無法載入租戶語系清單'
        });
      }
    });
  }

  /**
   * 常用範本選擇時自動填入表單
   */
  onPresetSelect(preset: any): void {
    if (preset) {
      this.formCode = preset.code;
      this.formName = preset.name;
      this.formDesc = preset.desc;
    }
  }

  /**
   * 儲存語系配置 (新增或更新)
   */
  saveLocale(): void {
    if (!this.formCode.trim() || !this.formName.trim()) {
      this.messageService.add({
        severity: 'warn',
        summary: '驗證提醒',
        detail: '語系代碼與顯示名稱不得為空'
      });
      return;
    }

    if (this.isEditing && this.editId) {
      const target = this.allTenantLocales.find((l) => l.id === this.editId);

      // 限制 1：若該語系為預設語系 (DEFAULT)，禁止停用
      if (target?.isDefault && this.formActiveFlag === 'N') {
        this.messageService.add({
          severity: 'warn',
          summary: '操作限制',
          detail: '租戶預設語系正在使用中，不得停用。請先將其他有效語系設為預設後再執行停用。'
        });
        return;
      }

      // 限制 2：若該語系為當前登入者正在使用的語系，限制停用
      if (this.formCode.toLowerCase() === this.currentLocaleCode.toLowerCase() && this.formActiveFlag === 'N') {
        this.messageService.add({
          severity: 'warn',
          summary: '操作限制',
          detail: `語系 [${this.formCode}] 為您目前正在使用之語系，請先切換至其他可用語系後再進行停用。`
        });
        return;
      }

      // 限制 3：最後一個有效語系不可停用
      const activeCount = this.allTenantLocales.filter((l) => l.activeFlag === 'Y').length;
      if (activeCount <= 1 && target?.activeFlag === 'Y' && this.formActiveFlag === 'N') {
        this.messageService.add({
          severity: 'warn',
          summary: '操作限制',
          detail: '租戶至少必須保留一種有效語系，不可停用最後一個有效語系。'
        });
        return;
      }

      this.localeService
        .updateLocale(this.editId, {
          name: this.formName,
          description: this.formDesc,
          isDefault: this.formIsDefault,
          priorityNo: this.formPriorityNo,
          activeFlag: this.formActiveFlag
        }, this.currentTenant)
        .subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: '更新成功',
              detail: `語系 [${this.formCode}] 已更新`
            });
            this.resetLocaleForm();
            this.loadAllTenantLocales();
            this.loadTenantLocales();
          },
          error: (err) => {
            console.error('Update locale error', err);
            this.messageService.add({
              severity: 'error',
              summary: '更新失敗',
              detail: '無法更新指定語系'
            });
          }
        });
    } else {
      this.localeService
        .createLocale(this.currentTenant, {
          code: this.formCode,
          name: this.formName,
          description: this.formDesc,
          isDefault: this.formIsDefault,
          priorityNo: this.formPriorityNo
        })
        .subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: '建立成功',
              detail: `已為租戶 [${this.currentTenant}] 建立語系 [${this.formCode}]`
            });
            this.resetLocaleForm();
            this.loadAllTenantLocales();
            this.loadTenantLocales();
          },
          error: (err) => {
            console.error('Create locale error', err);
            this.messageService.add({
              severity: 'error',
              summary: '建立失敗',
              detail: '無法建立該語系項目，請檢查代碼是否重複。'
            });
          }
        });
    }
  }

  /**
   * 編輯指定語系
   */
  editLocale(loc: TenantLocaleItem): void {
    this.isEditing = true;
    this.editId = loc.id;
    this.formCode = loc.code;
    this.formName = loc.name;
    this.formDesc = loc.description || '';
    this.formIsDefault = loc.isDefault;
    this.formPriorityNo = loc.priorityNo;
    this.formActiveFlag = loc.activeFlag;
  }

  /**
   * 重置表單
   */
  resetLocaleForm(): void {
    this.isEditing = false;
    this.editId = null;
    this.formCode = '';
    this.formName = '';
    this.formDesc = '';
    this.formIsDefault = false;
    this.formPriorityNo = this.allTenantLocales.length + 1;
    this.formActiveFlag = 'Y';
    this.selectedPreset = null;
  }

  /**
   * 設定指定語系為預設
   */
  setDefaultLocale(loc: TenantLocaleItem): void {
    this.localeService.setLocaleDefault(loc.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: '設定成功',
          detail: `已將 [${loc.name}] 設為租戶 ${loc.tenant} 預設語系`
        });
        this.loadAllTenantLocales();
        this.loadTenantLocales();
      },
      error: (err) => {
        console.error('Set default locale error', err);
      }
    });
  }

  /**
   * 刪除指定語系
   */
  confirmDeleteLocale(loc: TenantLocaleItem): void {
    // 限制 1：若為預設語系，不可刪除
    if (loc.isDefault) {
      this.messageService.add({
        severity: 'warn',
        summary: '操作限制',
        detail: '租戶預設語系 (DEFAULT) 正在使用中，不得刪除。請先將其他有效語系設為預設後再刪除。'
      });
      return;
    }

    // 限制 2：當前登入者正在使用的語系不可刪除
    if (loc.code.toLowerCase() === this.currentLocaleCode.toLowerCase()) {
      this.messageService.add({
        severity: 'warn',
        summary: '操作限制',
        detail: `語系 [${loc.code}] 為您目前正在使用之語系，請先切換至其他可用語系後再進行刪除。`
      });
      return;
    }

    // 限制 3：最後一個有效語系不可刪除
    const activeCount = this.allTenantLocales.filter((l) => l.activeFlag === 'Y').length;
    if (activeCount <= 1 && loc.activeFlag === 'Y') {
      this.messageService.add({
        severity: 'warn',
        summary: '操作限制',
        detail: '租戶至少必須保留一種有效語系，不可刪除最後一個有效語系。'
      });
      return;
    }

    this.confirmationService.confirm({
      message: `確定要刪除租戶 [${loc.tenant}] 的語系設定「${loc.name} (${loc.code})」嗎？`,
      header: '刪除語系確認',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: '確定刪除',
      rejectLabel: '取消',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.localeService.deleteLocale(loc.id, this.currentTenant).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: '刪除成功',
              detail: `已移除語系 [${loc.code}]`
            });
            if (this.isEditing && this.editId === loc.id) {
              this.resetLocaleForm();
            }
            this.loadAllTenantLocales();
            this.loadTenantLocales();
          },
          error: (err) => {
            console.error('Delete locale error', err);
            this.messageService.add({
              severity: 'error',
              summary: '刪除失敗',
              detail: '無法刪除指定語系'
            });
          }
        });
      }
    });
  }
}
