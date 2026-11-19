import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';

import { PanelModule } from 'primeng/panel';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { InputTextarea } from 'primeng/inputtextarea';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
import { NavigationService } from '../../../../core/services/navigation.service';
import { DynamicPageService } from '../../../dynamic-page/services/dynamic-page.service';
import { FormField, FormFieldOption } from '../../../dynamic-page/models/form-field.model';

/**
 * 租戶畫像動態頁面元件 (TenantProfileComponent)
 * <p>
 * 核心功能：
 * 1. 直接根據 Schema 架構 (TENANT_PROFILE_SCHEMA) 在頁面上動態渲染完整的 UI 表單。
 * 2. 查出回填編輯 (Query & In-place Edit)：
 *    - 頁面載入時自動從後端查出當前租戶已儲存之畫像資料並直接回填於表單中供即時編輯。
 *    - 支援頂部「查詢/切換租戶」工具列，輸入或選擇租戶代碼後點擊「查出資料」，立即從後端載入該租戶內容至表單。
 * 3. 儲存與重設：使用者在頁面上直接修改欄位後點選「儲存畫像設定」，直接持久化至後端並提示成功。
 * 4. 支援「修改欄位結構 (Designer)」一鍵跳轉至設計器調整動態欄位。
 * </p>
 */
@Component({
  selector: 'app-tenant-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PanelModule,
    InputTextModule,
    InputNumberModule,
    DropdownModule,
    CalendarModule,
    AutoCompleteModule,
    InputTextarea,
    ButtonModule,
    ToastModule,
    TooltipModule,
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

  // 查詢與租戶切換
  public queryTenant: string = 'CW';
  public readonly availableTenants = [
    { label: '預設基準租戶 (CW)', value: 'CW' },
    { label: '國泰金融租戶 (CTBC)', value: 'CTBC' },
    { label: '北美企業租戶 (ACME)', value: 'ACME' },
    { label: '示範測試租戶 (DEMO)', value: 'DEMO' }
  ];

  // 動態 Schema 欄位與表單模型
  public formFields: FormField[] = [];
  public formGroup: FormGroup = new FormGroup({});

  // 狀態旗標
  public loading: boolean = false;
  public saving: boolean = false;
  public submitted: boolean = false;
  public lastLoadedData: any = null;
  public isLivePreviewCollapsed: boolean = false;

  // AutoComplete 建議項目暫存清單
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
    this.currentTenant = this.tenantService.getCurrentTenant() || 'CW';
    this.queryTenant = this.currentTenant;

    // 訂閱全域租戶變更
    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.currentTenant = tenant;
      this.queryTenant = tenant;
      this.loadPageAndQueryData();
    });

    this.loadPageAndQueryData();
  }

  ngOnDestroy(): void {
    if (this.tenantSub) {
      this.tenantSub.unsubscribe();
    }
  }

  /**
   * 載入動態 Schema 架構，並自動查出該租戶已儲存之資料進行回填 (查出來編輯)
   */
  public loadPageAndQueryData(): void {
    this.loading = true;
    this.submitted = false;

    // 1. 取得動態頁面 Schema (欄位定義)
    this.dynamicPageService.getPageSchema(this.schemaCode, this.queryTenant).subscribe({
      next: (fields) => {
        this.formFields = fields;
        this.buildFormControls(fields);

        // 2. 查出指定租戶已儲存的表單資料 (查出來回填)
        this.queryTenantProfileData();
      },
      error: (err) => {
        console.error('Failed to load page schema', err);
        this.messageService.add({
          severity: 'error',
          summary: '載入結構失敗',
          detail: '無法取得 TENANT_PROFILE_SCHEMA 動態欄位結構'
        });
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 查出指定租戶的資料並回填到表單中
   */
  public queryTenantProfileData(): void {
    this.loading = true;
    const targetTenant = this.queryTenant?.trim() || this.currentTenant;

    this.dynamicPageService.getPageData(this.pageCode, targetTenant).subscribe({
      next: (data) => {
        this.loading = false;
        if (data && Object.keys(data).length > 0) {
          this.lastLoadedData = { ...data };
          this.formGroup.patchValue(data);
          this.messageService.add({
            severity: 'info',
            summary: '資料查出成功',
            detail: `已成功查出租戶 [${targetTenant}] 之畫像資料並回填至表單`
          });
        } else {
          // 若無資料，以初始預設值填入基本欄位
          const defaultData = {
            tenantCode: targetTenant,
            tenantName: targetTenant === 'CW' ? '國泰世華銀行總行' : `客製租戶空間 (${targetTenant})`,
            contactEmail: targetTenant === 'CW' ? 'admin@cw.com' : `admin@${targetTenant.toLowerCase()}.com`,
            maxUsers: 500,
            tier: 'ENTERPRISE',
            contractExpiry: '2027-12-31',
            dataRegion: 'TW-TPE-1',
            remarks: '核心商業租戶，支援高可用性與多可用區數據隔離。'
          };
          this.lastLoadedData = { ...defaultData };
          this.formGroup.patchValue(defaultData);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to query tenant profile data', err);
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 依據 Schema 欄位清單動態建構 Reactive FormControls
   */
  private buildFormControls(fields: FormField[]): void {
    const controls: Record<string, any> = {};

    fields.forEach((field) => {
      const validators = field.validators ? this.getValidators(field.validators) : [];
      controls[field.name] = ['', validators];
    });

    this.formGroup = this.fb.group(controls);
  }

  /**
   * 轉換欄位驗證設定為 Angular Validators
   */
  private getValidators(validators: string[]) {
    const formValidators = [];
    if (validators.includes('required')) {
      formValidators.push(Validators.required);
    }
    if (validators.includes('email')) {
      formValidators.push(Validators.email);
    }
    return formValidators;
  }

  /**
   * 儲存表單資料至後端
   */
  public onSubmit(): void {
    this.submitted = true;

    if (this.formGroup.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: '表單驗證未通過',
        detail: '請檢查紅框標記之必填欄位或格式錯誤後再儲存'
      });
      return;
    }

    this.saving = true;
    const targetTenant = this.queryTenant?.trim() || this.currentTenant;
    const formData = this.formGroup.value;

    this.dynamicPageService.savePageData(this.pageCode, formData, targetTenant).subscribe({
      next: () => {
        this.saving = false;
        this.lastLoadedData = { ...formData };
        this.messageService.add({
          severity: 'success',
          summary: '儲存成功',
          detail: `租戶 [${targetTenant}] 之畫像資料已成功儲存並同步持久化！`
        });
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.saving = false;
        console.error('Failed to save tenant profile data', err);
        this.messageService.add({
          severity: 'error',
          summary: '儲存失敗',
          detail: '無法寫入租戶畫像資料，請確認後端服務正常運作'
        });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 重設還原為當前查出之資料
   */
  public resetToLoadedData(): void {
    if (this.lastLoadedData) {
      this.formGroup.patchValue(this.lastLoadedData);
      this.messageService.add({
        severity: 'info',
        summary: '已還原',
        detail: '表單已恢復為最後查出之數值'
      });
    } else {
      this.formGroup.reset();
    }
  }

  /**
   * 開新填寫空白表單
   */
  public startNewProfile(): void {
    this.formGroup.reset({
      tenantCode: this.queryTenant,
      maxUsers: 100,
      tier: 'STANDARD'
    });
    this.messageService.add({
      severity: 'info',
      summary: '空白表單已備妥',
      detail: '您現在可以直接輸入全新租戶畫像資料'
    });
  }

  /**
   * 跳轉前往「動態表單架構設計器 (Schema Designer)」
   */
  public goToSchemaDesigner(): void {
    this.navigationService.setView('dynamic-designer');
  }

  /**
   * 切換底部即時 JSON Inspector 展開/收合
   */
  public toggleLivePreview(): void {
    this.isLivePreviewCollapsed = !this.isLivePreviewCollapsed;
  }

  /**
   * 自動完成建議搜尋 (AutoComplete)
   */
  public searchSuggestions(event: { query: string }, uri?: string): void {
    const keyword = event.query || '';
    this.dynamicPageService.searchAutoComplete(uri || 'regions', keyword).subscribe({
      next: (options) => {
        this.allSuggestions = options;
        this.cdr.markForCheck();
      },
      error: () => {
        this.allSuggestions = [];
      }
    });
  }

  public formControl(fieldName: string) {
    return this.formGroup.get(fieldName);
  }

  public formControlInvalid(fieldName: string): boolean {
    const control = this.formGroup.get(fieldName);
    return !!(control && control.invalid && (control.dirty || control.touched || this.submitted));
  }
}
