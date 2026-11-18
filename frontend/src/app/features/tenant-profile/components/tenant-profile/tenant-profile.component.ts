import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
import { NavigationService } from '../../../../core/services/navigation.service';
import { DynamicPageService } from '../../../dynamic-page/services/dynamic-page.service';
import { FormField, FormFieldOption } from '../../../dynamic-page/models/form-field.model';

/**
 * 租戶畫像資料管理元件 (TenantProfileComponent)
 * <p>
 * 專門針對 TENANT_PROFILE_SCHEMA 所構建的業務功能元件：
 * 1. 自動自後端載入目前租戶已定義的 TENANT_PROFILE_SCHEMA 結構。
 * 2. 依據 Schema 欄位定義動態產生相對應之輸入控制項與驗證邏輯。
 * 3. 租戶切換時即刻切換數據空間，保存各租戶自訂之 TENANT_PROFILE_DATA。
 * 4. 提供一鍵跳轉「動態表單設計器」以隨時自訂與擴充欄位架構。
 * </p>
 */
@Component({
  selector: 'app-tenant-profile',
  standalone: true,
  imports: [
    CommonModule,
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
    CardModule,
    TagModule,
    TooltipModule
  ],
  providers: [MessageService],
  templateUrl: './tenant-profile.component.html',
  styleUrl: './tenant-profile.component.scss'
})
export class TenantProfileComponent implements OnInit, OnDestroy {
  public formGroup: FormGroup = new FormGroup({});
  public formFields: FormField[] = [];
  public currentTenant: string = 'CW';

  public readonly pageCode: string = 'TENANT_PROFILE';
  public readonly schemaCode: string = 'TENANT_PROFILE_SCHEMA';

  public submitted: boolean = false;
  public loading: boolean = false;
  public saving: boolean = false;

  public allSuggestions: FormFieldOption[] = [];
  public lastSavedData: any = null;

  private tenantSub!: Subscription;

  constructor(
    private fb: FormBuilder,
    private dynamicPageService: DynamicPageService,
    private tenantService: TenantService,
    private navigationService: NavigationService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentTenant = this.tenantService.getCurrentTenant();

    // 訂閱當前租戶變更，即時重新抓取對應租戶的 Schema 與 Data
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
   * 載入當前租戶之 TENANT_PROFILE_SCHEMA 欄位架構與填寫資料
   */
  public loadTenantProfile(): void {
    this.loading = true;
    this.submitted = false;

    // 1. 取得後端 TENANT_PROFILE_SCHEMA
    this.dynamicPageService.getPageSchema(this.pageCode, this.currentTenant).subscribe({
      next: (fields) => {
        this.formFields = fields || [];
        this.buildFormControls(this.formFields);

        // 2. 取得後端已儲存的 TENANT_PROFILE 填寫資料
        this.dynamicPageService.getPageData(this.pageCode, this.currentTenant).subscribe({
          next: (data) => {
            this.lastSavedData = data;
            if (data && Object.keys(data).length > 0) {
              this.formGroup.patchValue(data);
            } else {
              // 若尚無資料，自動代入預設識別資訊
              this.formGroup.patchValue({
                tenantCode: this.currentTenant,
                tenantName: this.currentTenant === 'CW' ? '國泰世華核心租戶' : `客製租戶空間 (${this.currentTenant})`
              });
            }
            this.loading = false;
            this.cdr.markForCheck();
          },
          error: (err) => {
            console.error('Failed to load tenant profile data', err);
            this.loading = false;
            this.cdr.markForCheck();
          }
        });
      },
      error: (err) => {
        console.error('Failed to load TENANT_PROFILE_SCHEMA', err);
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 動態依據 Schema 建構 FormGroup 與驗證器
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
   * 解析 Validators
   */
  private getValidators(validators: string[]) {
    const formValidators = [];
    if (validators.includes('required')) {
      formValidators.push(Validators.required);
    }
    if (validators.includes('email')) {
      formValidators.push(Validators.email);
    }
    if (validators.includes('requiredTrue')) {
      formValidators.push(Validators.requiredTrue);
    }
    return formValidators;
  }

  public formControl(name: string) {
    return this.formGroup.get(name);
  }

  public formControlInvalid(name: string): boolean {
    const control = this.formGroup.get(name);
    return Boolean(control && control.invalid && (control.dirty || control.touched || this.submitted));
  }

  /**
   * 提交保存租戶基本資料
   */
  public onSubmit(): void {
    this.submitted = true;
    if (this.formGroup.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: '表單檢查未通過',
        detail: '請檢查並填寫所有必填欄位，或修正格式不符之輸入！'
      });
      return;
    }

    this.saving = true;
    const formValue = this.formGroup.value;

    this.dynamicPageService.savePageData(this.pageCode, formValue, this.currentTenant).subscribe({
      next: () => {
        this.saving = false;
        this.lastSavedData = formValue;
        this.messageService.add({
          severity: 'success',
          summary: '儲存成功',
          detail: `租戶 [${this.currentTenant}] 之 TENANT_PROFILE 資料已順利保存！`
        });
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.saving = false;
        this.messageService.add({
          severity: 'error',
          summary: '儲存失敗',
          detail: '保存失敗，請確認資料庫連線或欄位長度。'
        });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * AutoComplete 建議選項查詢
   */
  public search(event: any, uri?: string): void {
    this.dynamicPageService.searchAutoComplete(event.query, uri || '').subscribe({
      next: (suggestions) => {
        this.allSuggestions = suggestions;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 前往動態表單設計器自訂 Schema 欄位
   */
  public goToSchemaDesigner(): void {
    this.navigationService.setView('dynamic-designer');
  }

  /**
   * 重設表單
   */
  public clear(): void {
    this.formGroup.reset();
    this.submitted = false;
  }
}
