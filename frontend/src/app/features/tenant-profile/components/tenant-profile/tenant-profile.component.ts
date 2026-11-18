import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';

import { TableModule } from 'primeng/table';
import { PanelModule } from 'primeng/panel';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { InputTextarea } from 'primeng/inputtextarea';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
import { NavigationService } from '../../../../core/services/navigation.service';
import { DynamicPageService } from '../../../dynamic-page/services/dynamic-page.service';
import { FormField, FormFieldOption } from '../../../dynamic-page/models/form-field.model';

/**
 * 租戶畫像資料管理元件 (TenantProfileComponent)
 * <p>
 * 遵循完整 CRUD 設計規範：
 * 1. 查詢條件區：關鍵字搜尋與方案等級過濾，支援即時查出資料。
 * 2. 清單表格區：列出目前已建檔之租戶畫像資料，每列皆有「編輯 (查出資料回填)」、「檢視」與「刪除」。
 * 3. 動態表單 Dialog：依據 TENANT_PROFILE_SCHEMA 自動生成，點擊「新增」或「編輯」時彈出並支援驗證與保存。
 * </p>
 */
@Component({
  selector: 'app-tenant-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TableModule,
    PanelModule,
    InputTextModule,
    InputNumberModule,
    DropdownModule,
    CalendarModule,
    AutoCompleteModule,
    InputTextarea,
    ButtonModule,
    ToastModule,
    CardModule,
    TagModule,
    TooltipModule,
    DialogModule,
    ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './tenant-profile.component.html',
  styleUrl: './tenant-profile.component.scss'
})
export class TenantProfileComponent implements OnInit, OnDestroy {
  public currentTenant: string = 'CW';
  public readonly pageCode: string = 'TENANT_PROFILE';
  public readonly schemaCode: string = 'TENANT_PROFILE_SCHEMA';

  // 動態 Schema 欄位
  public formFields: FormField[] = [];
  public formGroup: FormGroup = new FormGroup({});

  // 查詢條件模型
  public searchKeyword: string = '';
  public selectedTier: string = 'ALL';
  public readonly tierFilterOptions = [
    { label: '全部方案等級', value: 'ALL' },
    { label: '標準版 (STANDARD)', value: 'STANDARD' },
    { label: '企業版 (ENTERPRISE)', value: 'ENTERPRISE' },
    { label: '旗艦版 (ULTIMATE)', value: 'ULTIMATE' }
  ];

  // 資料列表狀態
  public allRecords: any[] = [];
  public filteredRecords: any[] = [];
  public loading: boolean = false;
  public saving: boolean = false;

  // 動態表單 Dialog 狀態 (新增 / 編輯)
  public formDialogVisible: boolean = false;
  public isEditMode: boolean = false;
  public currentEditingIndex: number = -1;
  public submitted: boolean = false;

  // 詳情檢視 Dialog 狀態
  public viewDialogVisible: boolean = false;
  public selectedRecordForView: any = null;

  public allSuggestions: FormFieldOption[] = [];

  private tenantSub!: Subscription;

  constructor(
    private fb: FormBuilder,
    private dynamicPageService: DynamicPageService,
    private tenantService: TenantService,
    private navigationService: NavigationService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentTenant = this.tenantService.getCurrentTenant();

    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.currentTenant = tenant;
      this.loadTenantProfile();
    });
  }

  ngOnDestroy(): void {
    if (this.tenantSub) {
      this.tenantSub.unsubscribe();
    }
  }

  /**
   * 載入當前租戶之 TENANT_PROFILE_SCHEMA 結構與資料清單
   */
  public loadTenantProfile(): void {
    this.loading = true;

    // 1. 取得後端 TENANT_PROFILE_SCHEMA
    this.dynamicPageService.getPageSchema(this.pageCode, this.currentTenant).subscribe({
      next: (fields) => {
        this.formFields = fields || [];
        this.buildFormControls(this.formFields);

        // 2. 查出後端已儲存的租戶資料列表
        this.dynamicPageService.getPageRecords(this.pageCode, this.currentTenant).subscribe({
          next: (records) => {
            this.allRecords = records || [];
            this.applyFilter();
            this.loading = false;
            this.cdr.markForCheck();
          },
          error: (err) => {
            console.error('Failed to load records', err);
            this.loading = false;
            this.cdr.markForCheck();
          }
        });
      },
      error: (err) => {
        console.error('Failed to load schema', err);
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 執行查詢過濾
   */
  public onSearch(): void {
    this.applyFilter();
    this.messageService.add({
      severity: 'info',
      summary: '查詢完成',
      detail: `共查出 ${this.filteredRecords.length} 筆租戶畫像資料`
    });
  }

  /**
   * 重設查詢條件
   */
  public onReset(): void {
    this.searchKeyword = '';
    this.selectedTier = 'ALL';
    this.applyFilter();
  }

  private applyFilter(): void {
    let result = [...this.allRecords];

    if (this.searchKeyword && this.searchKeyword.trim()) {
      const kw = this.searchKeyword.trim().toLowerCase();
      result = result.filter((r) => {
        const code = String(r.tenantCode || '').toLowerCase();
        const name = String(r.tenantName || '').toLowerCase();
        const email = String(r.contactEmail || '').toLowerCase();
        return code.includes(kw) || name.includes(kw) || email.includes(kw);
      });
    }

    if (this.selectedTier && this.selectedTier !== 'ALL') {
      result = result.filter((r) => r.tier === this.selectedTier);
    }

    this.filteredRecords = result;
  }

  /**
   * 開啟新增對話框
   */
  public openCreateDialog(): void {
    this.isEditMode = false;
    this.currentEditingIndex = -1;
    this.submitted = false;
    this.formGroup.reset();

    // 賦予預設值
    this.formGroup.patchValue({
      tenantCode: this.currentTenant,
      tier: 'STANDARD',
      maxUsers: 100
    });

    this.formDialogVisible = true;
  }

  /**
   * 查出特定資料並開啟編輯對話框 (核心：查出來編輯)
   */
  public openEditDialog(record: any, index: number): void {
    this.isEditMode = true;
    this.currentEditingIndex = index;
    this.submitted = false;

    // 將該筆查出的資料完整回填至動態生成之 Form 控制項
    this.formGroup.patchValue(record);
    this.formDialogVisible = true;

    this.messageService.add({
      severity: 'info',
      summary: '已查出資料進行編輯',
      detail: `已載入租戶 [${record.tenantName || record.tenantCode}] 之完整畫像資料`
    });
  }

  /**
   * 檢視詳細資料
   */
  public openViewDialog(record: any): void {
    this.selectedRecordForView = record;
    this.viewDialogVisible = true;
  }

  /**
   * 儲存動態表單資料 (新增或更新)
   */
  public saveFormDialog(): void {
    this.submitted = true;
    if (this.formGroup.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: '表單驗證未通過',
        detail: '請檢查並填寫所有必填欄位或修正格式錯誤！'
      });
      return;
    }

    this.saving = true;
    const formValue = this.formGroup.value;

    if (this.isEditMode && this.currentEditingIndex >= 0) {
      this.allRecords[this.currentEditingIndex] = { ...this.allRecords[this.currentEditingIndex], ...formValue };
    } else {
      this.allRecords.unshift(formValue);
    }

    this.dynamicPageService.savePageRecords(this.pageCode, this.allRecords, this.currentTenant).subscribe({
      next: () => {
        this.saving = false;
        this.formDialogVisible = false;
        this.applyFilter();
        this.messageService.add({
          severity: 'success',
          summary: '儲存成功',
          detail: `租戶 [${formValue.tenantName || formValue.tenantCode}] 資料已更新保存！`
        });
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.saving = false;
        this.messageService.add({
          severity: 'error',
          summary: '儲存失敗',
          detail: '無法保存資料，請確認後端連線或欄位長度。'
        });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 刪除資料紀錄
   */
  public confirmDelete(record: any, index: number): void {
    this.confirmationService.confirm({
      message: `確認要刪除租戶「${record.tenantName || record.tenantCode}」的畫像資料嗎？`,
      header: '刪除確認',
      icon: 'pi pi-trash',
      acceptLabel: '確定刪除',
      rejectLabel: '取消',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.allRecords.splice(index, 1);
        this.dynamicPageService.savePageRecords(this.pageCode, this.allRecords, this.currentTenant).subscribe({
          next: () => {
            this.applyFilter();
            this.messageService.add({
              severity: 'info',
              summary: '已刪除資料',
              detail: `已移除「${record.tenantName || record.tenantCode}」`
            });
            this.cdr.markForCheck();
          }
        });
      }
    });
  }

  /**
   * 動態建構 FormGroup 與 Validators
   */
  private buildFormControls(fields: FormField[]): void {
    const controls: Record<string, any> = {};

    fields.forEach((field) => {
      const validators = field.validators ? this.getValidators(field.validators) : [];
      controls[field.name] = ['', validators];
    });

    this.formGroup = this.fb.group(controls);
  }

  private getValidators(validators: string[]) {
    const formValidators = [];
    if (validators.includes('required')) formValidators.push(Validators.required);
    if (validators.includes('email')) formValidators.push(Validators.email);
    if (validators.includes('requiredTrue')) formValidators.push(Validators.requiredTrue);
    return formValidators;
  }

  public formControl(name: string) {
    return this.formGroup.get(name);
  }

  public formControlInvalid(name: string): boolean {
    const control = this.formGroup.get(name);
    return Boolean(control && control.invalid && (control.dirty || control.touched || this.submitted));
  }

  public search(event: any, uri?: string): void {
    this.dynamicPageService.searchAutoComplete(event.query, uri || '').subscribe({
      next: (suggestions) => {
        this.allSuggestions = suggestions;
        this.cdr.markForCheck();
      }
    });
  }

  public goToSchemaDesigner(): void {
    this.navigationService.setView('dynamic-designer');
  }

  public getTierBadgeSeverity(tier: string): "success" | "secondary" | "info" | "warn" | "danger" | "contrast" | undefined {
    switch (tier) {
      case 'ULTIMATE': return 'danger';
      case 'ENTERPRISE': return 'success';
      case 'STANDARD': return 'info';
      default: return 'secondary';
    }
  }
}
