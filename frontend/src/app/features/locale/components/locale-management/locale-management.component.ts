import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { CheckboxModule } from 'primeng/checkbox';
import { TagModule } from 'primeng/tag';
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
import { LocaleService, TenantLocaleItem } from '../../../../core/services/locale.service';

/**
 * 常用官方國際化語系範本
 */
interface QuickLocaleTemplate {
  code: string;
  name: string;
  description: string;
}

/**
 * 系統租戶語系配置維護頁面元件 (LocaleManagementComponent)
 * <p>
 * 遵循 Function Configurable 設計規範，管理指定租戶下 category=LOCALE 之所有語系項目。
 * 支援多租戶隔離、預設語系設定、即時套用系統語系、常用語系範本一鍵帶入與完整 CRUD 維護。
 * </p>
 */
@Component({
  selector: 'app-locale-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    DialogModule,
    DropdownModule,
    InputNumberModule,
    ToastModule,
    ConfirmDialogModule,
    TooltipModule,
    CheckboxModule,
    TagModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './locale-management.component.html',
  styleUrl: './locale-management.component.scss'
})
export class LocaleManagementComponent implements OnInit, OnDestroy {
  public currentTenant: string = 'CW';
  public currentActiveLocaleCode: string = 'zh-TW';

  // 語系列表資料
  public allLocales: TenantLocaleItem[] = [];
  public filteredLocales: TenantLocaleItem[] = [];
  public loading: boolean = false;
  public saving: boolean = false;

  // 查詢與過濾
  public searchKeyword: string = '';
  public selectedStatus: string = 'ALL';
  public readonly statusFilterOptions = [
    { label: '全部狀態', value: 'ALL' },
    { label: '僅有效 (Active)', value: 'Y' },
    { label: '僅停用 (Inactive)', value: 'N' }
  ];

  // 常用官方語系範本清單
  public readonly quickTemplates: QuickLocaleTemplate[] = [
    { code: 'zh-TW', name: '繁體中文', description: '繁體中文 (台灣)' },
    { code: 'zh-CN', name: '簡體中文', description: '簡體中文 (中國大陸)' },
    { code: 'en-US', name: 'English', description: 'English (United States)' },
    { code: 'ja-JP', name: '日本語', description: '日本語 (日本)' },
    { code: 'ko-KR', name: '한국어', description: '한국어 (대한민국)' },
    { code: 'de-DE', name: 'Deutsch', description: 'Deutsch (Deutschland)' },
    { code: 'fr-FR', name: 'Français', description: 'Français (France)' }
  ];

  // 新增 / 編輯對話框狀態
  public dialogVisible: boolean = false;
  public isEditMode: boolean = false;
  public selectedTemplate: QuickLocaleTemplate | null = null;
  public formModel = {
    id: 0,
    code: '',
    name: '',
    description: '',
    priorityNo: 1,
    isDefault: false,
    activeFlag: 'Y'
  };

  public readonly activeFlagOptions = [
    { label: '有效 (Y)', value: 'Y' },
    { label: '停用 (N)', value: 'N' }
  ];

  private tenantSub!: Subscription;
  private localeSub!: Subscription;

  constructor(
    private localeService: LocaleService,
    private tenantService: TenantService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentTenant = this.tenantService.getCurrentTenant() || 'CW';
    this.currentActiveLocaleCode = this.localeService.getCurrentLocale();

    // 訂閱當前租戶變更
    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.currentTenant = tenant;
      this.loadLocales();
    });

    // 訂閱當前系統選用語系
    this.localeSub = this.localeService.currentLocale$.subscribe((code) => {
      this.currentActiveLocaleCode = code;
      this.cdr.markForCheck();
    });

    this.loadLocales();
  }

  ngOnDestroy(): void {
    if (this.tenantSub) this.tenantSub.unsubscribe();
    if (this.localeSub) this.localeSub.unsubscribe();
  }

  /**
   * 載入當前租戶所有語系列表 (包含有效與已停用)
   */
  public loadLocales(): void {
    this.loading = true;
    this.localeService.getAllTenantLocales(this.currentTenant).subscribe({
      next: (locales) => {
        this.allLocales = locales || [];
        this.filterLocales();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        console.error('Failed to load tenant locales', err);
        this.messageService.add({
          severity: 'error',
          summary: '載入失敗',
          detail: '無法取得該租戶之語系清單'
        });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 前端本機篩選 (關鍵字與啟用狀態)
   */
  public filterLocales(): void {
    let result = [...this.allLocales];

    // 1. 狀態篩選
    if (this.selectedStatus && this.selectedStatus !== 'ALL') {
      result = result.filter((l) => l.activeFlag === this.selectedStatus);
    }

    // 2. 關鍵字過濾 (代碼、名稱、描述)
    if (this.searchKeyword && this.searchKeyword.trim()) {
      const kw = this.searchKeyword.trim().toLowerCase();
      result = result.filter(
        (l) =>
          l.code.toLowerCase().includes(kw) ||
          l.name.toLowerCase().includes(kw) ||
          (l.description && l.description.toLowerCase().includes(kw))
      );
    }

    // 依 priorityNo 排序
    result.sort((a, b) => (a.priorityNo || 0) - (b.priorityNo || 0));
    this.filteredLocales = result;
  }

  public onSearch(): void {
    this.filterLocales();
  }

  public onResetSearch(): void {
    this.searchKeyword = '';
    this.selectedStatus = 'ALL';
    this.filterLocales();
  }

  /**
   * 開啟新增語系對話框
   */
  public openCreateDialog(template?: QuickLocaleTemplate): void {
    this.isEditMode = false;
    this.selectedTemplate = template || null;

    const nextPriority = this.allLocales.length > 0
      ? Math.max(...this.allLocales.map((l) => l.priorityNo || 0)) + 1
      : 1;

    this.formModel = {
      id: 0,
      code: template ? template.code : '',
      name: template ? template.name : '',
      description: template ? template.description : '',
      priorityNo: nextPriority,
      isDefault: false,
      activeFlag: 'Y'
    };
    this.dialogVisible = true;
  }

  /**
   * 開啟編輯語系對話框
   */
  public openEditDialog(item: TenantLocaleItem): void {
    this.isEditMode = true;
    this.selectedTemplate = null;
    this.formModel = {
      id: item.id,
      code: item.code,
      name: item.name,
      description: item.description || '',
      priorityNo: item.priorityNo || 1,
      isDefault: item.isDefault,
      activeFlag: item.activeFlag
    };
    this.dialogVisible = true;
  }

  /**
   * 選擇常用範本時自動填入表單
   */
  public onTemplateChange(): void {
    if (this.selectedTemplate) {
      this.formModel.code = this.selectedTemplate.code;
      this.formModel.name = this.selectedTemplate.name;
      this.formModel.description = this.selectedTemplate.description;
    }
  }

  /**
   * 儲存語系設定 (新增 / 編輯)
   */
  public saveLocale(): void {
    if (!this.formModel.code || !this.formModel.code.trim()) {
      this.messageService.add({
        severity: 'warn',
        summary: '請輸入語系代碼',
        detail: '語系代碼為必填項目 (例如: zh-TW, en-US)'
      });
      return;
    }

    if (!this.formModel.name || !this.formModel.name.trim()) {
      this.messageService.add({
        severity: 'warn',
        summary: '請輸入語系名稱',
        detail: '語系顯示名稱為必填項目 (例如: 繁體中文)'
      });
      return;
    }

    this.saving = true;

    if (this.isEditMode) {
      // 編輯既有語系
      this.localeService
        .updateLocale(
          this.formModel.id,
          {
            name: this.formModel.name,
            description: this.formModel.description,
            isDefault: this.formModel.isDefault,
            priorityNo: this.formModel.priorityNo,
            activeFlag: this.formModel.activeFlag
          },
          this.currentTenant
        )
        .subscribe({
          next: () => {
            this.saving = false;
            this.dialogVisible = false;
            this.messageService.add({
              severity: 'success',
              summary: '更新成功',
              detail: `語系 [${this.formModel.code}] 已成功更新！`
            });
            this.loadLocales();
          },
          error: (err) => {
            this.saving = false;
            console.error('Failed to update locale', err);
            this.messageService.add({
              severity: 'error',
              summary: '更新失敗',
              detail: '無法更新語系配置'
            });
          }
        });
    } else {
      // 新增語系
      this.localeService
        .createLocale(this.currentTenant, {
          code: this.formModel.code,
          name: this.formModel.name,
          description: this.formModel.description,
          isDefault: this.formModel.isDefault,
          priorityNo: this.formModel.priorityNo
        })
        .subscribe({
          next: () => {
            this.saving = false;
            this.dialogVisible = false;
            this.messageService.add({
              severity: 'success',
              summary: '新增成功',
              detail: `已成功為租戶 [${this.currentTenant}] 新增語系：${this.formModel.name} (${this.formModel.code})`
            });
            this.loadLocales();
          },
          error: (err) => {
            this.saving = false;
            console.error('Failed to create locale', err);
            this.messageService.add({
              severity: 'error',
              summary: '新增失敗',
              detail: '無法新增語系，請確認該語系代碼是否已存在'
            });
          }
        });
    }
  }

  /**
   * 將指定語系設為該租戶之預設語系
   */
  public setDefault(item: TenantLocaleItem): void {
    if (item.isDefault) return;

    this.localeService.setLocaleDefault(item.id, this.currentTenant).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: '預設語系已設定',
          detail: `已將 [${item.name} (${item.code})] 設為租戶 [${this.currentTenant}] 之預設語系！`
        });
        this.loadLocales();
      },
      error: (err) => {
        console.error('Failed to set default locale', err);
        this.messageService.add({
          severity: 'error',
          summary: '設定失敗',
          detail: '無法變更預設語系'
        });
      }
    });
  }

  /**
   * 立即切換為當前系統語言 (並更新全域通知)
   */
  public applyLocale(item: TenantLocaleItem): void {
    this.localeService.setLocale(item.code);
    this.messageService.add({
      severity: 'info',
      summary: '系統語系已切換',
      detail: `系統當前使用語言已切換為：${item.name} (${item.code})`
    });
  }

  /**
   * 刪除/停用語系 (具備防呆確認)
   */
  public confirmDelete(item: TenantLocaleItem): void {
    if (item.isDefault) {
      this.messageService.add({
        severity: 'warn',
        summary: '無法刪除預設語系',
        detail: `[${item.name}] 是該租戶之預設語系，請先將其他語系設為預設後再刪除。`
      });
      return;
    }

    this.confirmationService.confirm({
      message: `確定要停用並刪除租戶 [${this.currentTenant}] 之語系「${item.name} (${item.code})」嗎？`,
      header: '刪除語系確認',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: '確定刪除',
      rejectLabel: '取消',
      acceptButtonStyleClass: 'p-button-danger p-button-sm',
      rejectButtonStyleClass: 'p-button-outlined p-button-secondary p-button-sm',
      accept: () => {
        this.localeService.deleteLocale(item.id, this.currentTenant).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: '刪除成功',
              detail: `語系 [${item.code}] 已成功移除！`
            });
            this.loadLocales();
          },
          error: (err) => {
            console.error('Failed to delete locale', err);
            this.messageService.add({
              severity: 'error',
              summary: '刪除失敗',
              detail: '無法刪除語系'
            });
          }
        });
      }
    });
  }

  // 取得統計數值
  public get activeCount(): number {
    return this.allLocales.filter((l) => l.activeFlag === 'Y').length;
  }

  public get defaultLocaleItem(): TenantLocaleItem | undefined {
    return this.allLocales.find((l) => l.isDefault);
  }
}
