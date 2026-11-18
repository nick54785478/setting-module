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
import { DynamicPageService } from '../../services/dynamic-page.service';
import { FormField, FormFieldOption } from '../../models/form-field.model';

/**
 * 租戶動態頁面表單元件 (DynamicPageComponent)
 * <p>
 * 遵循 Function Configurable 架構：
 * 1. 依據租戶動態載入客製化欄位定義與填寫資料。
 * 2. 支援 text, number, email, dropdown, calendar, autocomplete, textarea 多維度欄位型態。
 * 3. 租戶切換時即時響應、重新抓取並綁定對應租戶之聚合資料。
 * </p>
 */
@Component({
  selector: 'app-dynamic-page',
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
  templateUrl: './dynamic-page.component.html',
  styleUrl: './dynamic-page.component.scss'
})
export class DynamicPageComponent implements OnInit, OnDestroy {
  public formGroup: FormGroup = new FormGroup({});
  public formFields: FormField[] = [];
  public currentTenant: string = 'CW';
  public readonly pageCode: string = 'TENANT_PROFILE';

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
  ) { }

  ngOnInit(): void {
    this.currentTenant = this.tenantService.getCurrentTenant();

    // 訂閱租戶變更，即時切換載入不同租戶專屬之動態表單資料
    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.currentTenant = tenant;
      this.loadPageConfiguration();
    });
  }

  ngOnDestroy(): void {
    if (this.tenantSub) {
      this.tenantSub.unsubscribe();
    }
  }

  /**
   * 載入動態欄位架構與當前租戶之填寫資料
   */
  public loadPageConfiguration(): void {
    this.loading = true;
    this.submitted = false;

    // 1. 取得欄位 Schema 定義
    this.dynamicPageService.getPageSchema(this.pageCode, this.currentTenant).subscribe({
      next: (fields) => {
        this.formFields = fields;
        this.buildFormControls(fields);

        // 2. 取得當前租戶之表單資料
        this.dynamicPageService.getPageData(this.pageCode, this.currentTenant).subscribe({
          next: (data) => {
            this.lastSavedData = data;
            if (data && Object.keys(data).length > 0) {
              this.formGroup.patchValue(data);
            } else {
              // 若該租戶尚無資料，提供租戶預設識別填入
              this.formGroup.patchValue({
                tenantCode: this.currentTenant,
                tenantName: this.currentTenant === 'CW' ? 'CW' : `客製租戶 (${this.currentTenant})`
              });
            }
            this.loading = false;
            this.cdr.markForCheck();
          },
          error: (err) => {
            console.error('Failed to load dynamic page data', err);
            this.loading = false;
            this.cdr.markForCheck();
          }
        });
      },
      error: (err) => {
        console.error('Failed to load dynamic page schema', err);
        this.loading = false;
        this.cdr.markForCheck();
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

  /**
   * 轉換字串驗證器為 Angular Validators
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

  /**
   * 取得指定欄位之 FormControl
   */
  public formControl(name: string) {
    return this.formGroup.get(name);
  }

  /**
   * 檢查欄位是否驗證失敗
   */
  public formControlInvalid(name: string): boolean {
    const control = this.formGroup.get(name);
    return Boolean(control && control.invalid && (control.dirty || control.touched || this.submitted));
  }

  /**
   * 提交並儲存表單資料
   */
  public onSubmit(): void {
    this.submitted = true;
    if (this.formGroup.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: '表單驗證未通過',
        detail: '請檢查並填寫所有必填欄位或修正格式錯誤之內容！'
      });
      return;
    }

    this.saving = true;
    const formValue = this.formGroup.value;

    this.dynamicPageService.savePageData(this.pageCode, formValue, this.currentTenant).subscribe({
      next: (res) => {
        this.saving = false;
        this.lastSavedData = formValue;
        this.messageService.add({
          severity: 'success',
          summary: '儲存成功',
          detail: `租戶 [${this.currentTenant}] 專屬動態頁面配置已成功保存！`
        });
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.saving = false;
        this.messageService.add({
          severity: 'error',
          summary: '儲存失敗',
          detail: '無法保存動態頁面資料，請稍後再試。'
        });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * AutoComplete 搜尋聯想資料
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
   * 前往表單設計器自訂欄位
   */
  public goToDesigner(): void {
    this.navigationService.setView('dynamic-designer');
  }

  /**
   * 清除重置表單
   */
  public clear(): void {
    this.formGroup.reset();
    this.submitted = false;
  }
}
