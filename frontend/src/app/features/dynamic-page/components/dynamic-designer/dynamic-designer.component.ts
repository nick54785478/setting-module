import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { CheckboxModule } from 'primeng/checkbox';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { CardModule } from 'primeng/card';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
import { NavigationService } from '../../../../core/services/navigation.service';
import { DynamicPageService } from '../../services/dynamic-page.service';
import { FormField, FormFieldOption } from '../../models/form-field.model';

/**
 * 動態表單自訂設計器元件 (DynamicDesignerComponent)
 * <p>
 * 專為租戶管理者打造的視覺化表單架構設計器：
 * 1. 獨立維護各租戶專屬的表單 Schema (FormField[] 聚合 JSON)。
 * 2. 支援視覺化新增、編輯、上下排序、刪除欄位。
 * 3. 支援配置多種元件型別 (text, number, email, dropdown, calendar, autocomplete, textarea) 與自訂下拉選項。
 * 4. 變更後即時同步更新至後端 /pages/{pageCode}/schema。
 * </p>
 */
@Component({
  selector: 'app-dynamic-designer',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    DropdownModule,
    DialogModule,
    CheckboxModule,
    TagModule,
    ToastModule,
    TooltipModule,
    CardModule,
    ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './dynamic-designer.component.html',
  styleUrl: './dynamic-designer.component.scss'
})
export class DynamicDesignerComponent implements OnInit, OnDestroy {
  public currentTenant: string = 'CW';
  public readonly pageCode: string = 'TENANT_PROFILE';

  public fields: FormField[] = [];
  public loading: boolean = false;
  public saving: boolean = false;

  // 欄位編輯對話框狀態
  public fieldDialogVisible: boolean = false;
  public isEditMode: boolean = false;
  public currentFieldIndex: number = -1;

  // 編輯中欄位表單模型
  public formModel: {
    name: string;
    label: string;
    type: string;
    placeholder: string;
    isRequired: boolean;
    isEmail: boolean;
    pattern: string;
    rows: number;
    cols: number;
    uri: string;
    options: FormFieldOption[];
  } = this.getEmptyFormModel();

  // 下拉選項暫存輸入
  public newOptionLabel: string = '';
  public newOptionValue: string = '';

  // 支援的元件型態選單
  public readonly fieldTypeOptions = [
    { label: '單行文字 (Text)', value: 'text' },
    { label: '數值輸入 (Number)', value: 'number' },
    { label: '電子信箱 (Email)', value: 'email' },
    { label: '時間輸入 (Time)', value: 'time' },
    { label: '下拉選單 (Dropdown)', value: 'dropdown' },
    { label: '日期選擇 (Calendar)', value: 'calendar' },
    { label: '自動建議搜尋 (AutoComplete)', value: 'autocomplete' },
    { label: '多行文字框 (Textarea)', value: 'textarea' }
  ];

  private tenantSub!: Subscription;

  constructor(
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
      this.loadSchema();
    });
  }

  ngOnDestroy(): void {
    if (this.tenantSub) {
      this.tenantSub.unsubscribe();
    }
  }

  /**
   * 載入當前租戶之表單 Schema
   */
  public loadSchema(): void {
    this.loading = true;
    this.dynamicPageService.getPageSchema(this.pageCode, this.currentTenant).subscribe({
      next: (schema) => {
        this.fields = JSON.parse(JSON.stringify(schema));
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load schema', err);
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 儲存設計完成之 Schema 到後端
   */
  public saveSchema(): void {
    this.saving = true;
    this.dynamicPageService.savePageSchema(this.pageCode, this.fields, this.currentTenant).subscribe({
      next: () => {
        this.saving = false;
        this.messageService.add({
          severity: 'success',
          summary: '儲存成功',
          detail: `租戶 [${this.currentTenant}] 專屬表單設計 Schema 已成功更新保存！`
        });
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.saving = false;
        this.messageService.add({
          severity: 'error',
          summary: '儲存失敗',
          detail: '無法保存表單設計，請稍後再試。'
        });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 一鍵載入預設表單範本
   */
  public resetToDefaultTemplate(): void {
    this.confirmationService.confirm({
      message: '確認要將欄位結構重設為官方預設標準範本嗎？（未保存的自訂欄位將被覆蓋）',
      header: '載入預設範本確認',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: '確定載入',
      rejectLabel: '取消',
      accept: () => {
        this.fields = this.dynamicPageService.getDefaultSchema();
        this.messageService.add({
          severity: 'info',
          summary: '已載入預設範本',
          detail: '請點擊「儲存設計 Schema」以正式生效。'
        });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 開啟新增欄位對話框
   */
  public openAddFieldDialog(): void {
    this.isEditMode = false;
    this.currentFieldIndex = -1;
    this.formModel = this.getEmptyFormModel();
    this.newOptionLabel = '';
    this.newOptionValue = '';
    this.fieldDialogVisible = true;
  }

  /**
   * 開啟編輯欄位對話框
   */
  public openEditFieldDialog(field: FormField, index: number): void {
    this.isEditMode = true;
    this.currentFieldIndex = index;
    this.formModel = {
      name: field.name,
      label: field.label,
      type: field.type,
      placeholder: field.placeholder || '',
      isRequired: field.validators?.includes('required') || false,
      isEmail: field.validators?.includes('email') || false,
      pattern: field.pattern || 'yy-mm-dd',
      rows: Number(field.rows) || 3,
      cols: Number(field.cols) || 30,
      uri: field.uri || '',
      options: field.options ? JSON.parse(JSON.stringify(field.options)) : []
    };
    this.newOptionLabel = '';
    this.newOptionValue = '';
    this.fieldDialogVisible = true;
  }

  /**
   * 儲存對話框中的欄位配置
   */
  public saveFieldDialog(): void {
    if (!this.formModel.name || !this.formModel.label) {
      this.messageService.add({
        severity: 'warn',
        summary: '請填寫必填欄位',
        detail: '「欄位識別代碼」與「欄位標籤名稱」不能為空！'
      });
      return;
    }

    // 格式化欄位英文識別代碼 (去空白)
    const formattedName = this.formModel.name.trim();

    // 檢查欄位代碼是否重複 (非編輯自身時)
    const exists = this.fields.some(
      (f, idx) => f.name.toLowerCase() === formattedName.toLowerCase() && idx !== this.currentFieldIndex
    );
    if (exists) {
      this.messageService.add({
        severity: 'warn',
        summary: '代碼重複',
        detail: `欄位代碼「${formattedName}」已存在，請使用不同代碼！`
      });
      return;
    }

    // 組裝 Validators
    const validators: string[] = [];
    if (this.formModel.isRequired) validators.push('required');
    if (this.formModel.isEmail) validators.push('email');

    const fieldItem: FormField = {
      name: formattedName,
      label: this.formModel.label.trim(),
      type: this.formModel.type,
      validators,
      placeholder: this.formModel.placeholder?.trim() || ''
    };

    if (this.formModel.type === 'dropdown') {
      fieldItem.options = this.formModel.options || [];
    }
    if (this.formModel.type === 'calendar') {
      fieldItem.pattern = this.formModel.pattern || 'yy-mm-dd';
    }
    if (this.formModel.type === 'textarea') {
      fieldItem.rows = this.formModel.rows || 3;
      fieldItem.cols = this.formModel.cols || 30;
    }
    if (this.formModel.type === 'autocomplete') {
      fieldItem.uri = this.formModel.uri || 'regions';
    }

    if (this.isEditMode && this.currentFieldIndex >= 0) {
      this.fields[this.currentFieldIndex] = fieldItem;
      this.messageService.add({ severity: 'success', summary: '欄位已修改', detail: `已更新「${fieldItem.label}」` });
    } else {
      this.fields.push(fieldItem);
      this.messageService.add({ severity: 'success', summary: '欄位已新增', detail: `已新增「${fieldItem.label}」` });
    }

    this.fieldDialogVisible = false;
    this.cdr.markForCheck();
  }

  /**
   * 刪除欄位
   */
  public deleteField(index: number): void {
    const target = this.fields[index];
    this.confirmationService.confirm({
      message: `確認要刪除欄位「${target.label} (${target.name})」嗎？`,
      header: '刪除欄位確認',
      icon: 'pi pi-trash',
      acceptLabel: '刪除',
      rejectLabel: '取消',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.fields.splice(index, 1);
        this.messageService.add({ severity: 'info', summary: '欄位已移除', detail: `已自清單中移除「${target.label}」` });
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 欄位上移
   */
  public moveUp(index: number): void {
    if (index <= 0) return;
    const temp = this.fields[index];
    this.fields[index] = this.fields[index - 1];
    this.fields[index - 1] = temp;
    this.cdr.markForCheck();
  }

  /**
   * 欄位下移
   */
  public moveDown(index: number): void {
    if (index >= this.fields.length - 1) return;
    const temp = this.fields[index];
    this.fields[index] = this.fields[index + 1];
    this.fields[index + 1] = temp;
    this.cdr.markForCheck();
  }

  /**
   * 新增下拉選單選項
   */
  public addDropdownOption(): void {
    if (!this.newOptionLabel || !this.newOptionValue) {
      this.messageService.add({ severity: 'warn', summary: '選項不完整', detail: '請輸入選項顯示標籤與儲存值！' });
      return;
    }
    this.formModel.options.push({
      label: this.newOptionLabel.trim(),
      value: this.newOptionValue.trim()
    });
    this.newOptionLabel = '';
    this.newOptionValue = '';
  }

  /**
   * 移除下拉選單選項
   */
  public removeDropdownOption(index: number): void {
    this.formModel.options.splice(index, 1);
  }

  /**
   * 跳轉前往「租戶畫像資料」頁面預覽測試
   */
  public goToFormRunner(): void {
    this.navigationService.setView('tenant-profile');
  }

  /**
   * 取得元件型態徽章顏色
   */
  public getTypeBadgeSeverity(type: string): "success" | "secondary" | "info" | "warn" | "danger" | "contrast" | undefined {
    switch (type) {
      case 'text': return 'info';
      case 'number': return 'warn';
      case 'email': return 'contrast';
      case 'dropdown': return 'success';
      case 'calendar': return 'danger';
      case 'autocomplete': return 'secondary';
      case 'textarea': return 'contrast';
      default: return 'info';
    }
  }

  private getEmptyFormModel() {
    return {
      name: '',
      label: '',
      type: 'text',
      placeholder: '',
      isRequired: false,
      isEmail: false,
      pattern: 'yy-mm-dd',
      rows: 3,
      cols: 30,
      uri: 'regions',
      options: []
    };
  }
}
