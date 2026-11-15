import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { CardModule } from 'primeng/card';
import { InputNumberModule } from 'primeng/inputnumber';
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
import { SettingService } from '../../services/setting.service';
import {
  CreateSettingRequest,
  SettingItem,
  SettingOption,
  SettingQueryParams,
  UpdateSettingRequest
} from '../../models/setting.model';

/**
 * 設定維護主功能元件 (SettingManagementComponent)
 * <p>
 * 遵循 Features 層規範，封裝 Setting 領域動態分頁清單、二級聯動選單、新增與編輯對話框。
 * </p>
 */
@Component({
  selector: 'app-setting-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    DialogModule,
    TagModule,
    ToastModule,
    ConfirmDialogModule,
    TooltipModule,
    CardModule,
    InputNumberModule
  ],
  templateUrl: './setting-management.component.html',
  styleUrl: './setting-management.component.scss'
})
export class SettingManagementComponent implements OnInit, OnDestroy {
  private tenantSub!: Subscription;
  currentTenant: string = 'CW';

  // 篩選條件
  filterCategory: string = '';
  filterSubCategory: string = '';
  filterCode: string = '';
  filterName: string = '';
  filterActiveFlag: string = '';

  activeFlagOptions = [
    { label: '全部狀態', value: '' },
    { label: '有效 (Y)', value: 'Y' },
    { label: '停用 (N)', value: 'N' }
  ];

  // 選單選項清單
  categoryOptions: SettingOption[] = [];
  subCategoryOptions: SettingOption[] = [];

  // 表格資料與分頁
  settings: SettingItem[] = [];
  totalRecords: number = 0;
  loading: boolean = false;
  page: number = 0;
  pageSize: number = 10;

  // 新增 / 編輯彈窗狀態
  dialogVisible: boolean = false;
  isEditMode: boolean = false;
  editId: number | null = null;

  // 表單資料模型
  formTenant: string = '';
  formCategory: string = '';
  formSubCategory: string = '';
  formCode: string = '';
  formName: string = '';
  formDescription: string = '';
  formValue: string = '';
  formPriorityNo: number = 1;
  formActiveFlag: string = 'Y';

  // 表單下拉選項
  dialogCategoryOptions: { label: string; value: string }[] = [];
  dialogSubCategoryOptions: { label: string; value: string }[] = [];

  constructor(
    private settingService: SettingService,
    private tenantService: TenantService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    // 訂閱全域租戶變更
    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.currentTenant = tenant;
      this.loadCategoryOptions();
      this.resetFilter();
    });
  }

  ngOnDestroy(): void {
    if (this.tenantSub) {
      this.tenantSub.unsubscribe();
    }
  }

  /**
   * 載入 Category 選項清單 (由後端 API 統一提供單一真實來源)
   */
  loadCategoryOptions(): void {
    this.settingService.getCategories(this.currentTenant).subscribe({
      next: (options) => {
        this.categoryOptions = options;
        this.dialogCategoryOptions = options;
      },
      error: (err) => {
        console.error('Failed to load categories', err);
      }
    });
  }

  /**
   * 篩選器 Category 異動時，載入對應之 SubCategory 選項 (二級聯動，由後端 API 提供)
   */
  onFilterCategoryChange(): void {
    this.filterSubCategory = '';
    this.subCategoryOptions = [];
    if (this.filterCategory) {
      this.settingService.getSubCategories(this.filterCategory, this.currentTenant).subscribe({
        next: (options) => {
          this.subCategoryOptions = options;
        },
        error: (err) => {
          console.error('Failed to load subcategories', err);
        }
      });
    }
  }

  /**
   * 彈窗 Category 異動時載入 SubCategory (由後端 API 提供)
   */
  onDialogCategoryChange(): void {
    this.dialogSubCategoryOptions = [];
    if (this.formCategory) {
      this.settingService.getSubCategories(this.formCategory, this.currentTenant).subscribe({
        next: (options) => {
          this.dialogSubCategoryOptions = options;
        },
        error: (err) => {
          console.error('Failed to load dialog subcategories', err);
        }
      });
    }
  }

  /**
   * 表格分頁觸發 (Lazy Load)
   */
  onLazyLoad(event: TableLazyLoadEvent): void {
    const first = event.first ?? 0;
    const rows = event.rows ?? 10;
    this.page = Math.floor(first / rows);
    this.pageSize = rows;
    this.loadData();
  }

  /**
   * 載入設定清單 (分頁與動態條件)
   */
  loadData(): void {
    this.loading = true;
    const params: SettingQueryParams = {
      tenant: this.currentTenant !== 'ALL' ? this.currentTenant : undefined,
      category: this.filterCategory || undefined,
      subCategory: this.filterSubCategory || undefined,
      code: this.filterCode || undefined,
      name: this.filterName || undefined,
      activeFlag: this.filterActiveFlag || undefined,
      page: this.page,
      size: this.pageSize
    };

    this.settingService.getSummary(params).subscribe({
      next: (res) => {
        this.settings = res.content || [];
        this.totalRecords = res.totalElements || 0;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: '查詢失敗',
          detail: '無法載入設定資料，請確認後端服務是否已啟動。'
        });
      }
    });
  }

  /**
   * 搜尋按鈕
   */
  onSearch(): void {
    this.page = 0;
    this.loadData();
  }

  /**
   * 重置篩選條件
   */
  resetFilter(): void {
    this.filterCategory = '';
    this.filterSubCategory = '';
    this.filterCode = '';
    this.filterName = '';
    this.filterActiveFlag = '';
    this.subCategoryOptions = [];
    this.page = 0;
    this.loadData();
  }

  /**
   * 打開新增彈窗
   */
  openCreateDialog(): void {
    this.isEditMode = false;
    this.editId = null;
    this.formTenant = this.currentTenant;
    this.formCategory = '';
    this.formSubCategory = '';
    this.formCode = '';
    this.formName = '';
    this.formDescription = '';
    this.formValue = '';
    this.formPriorityNo = 1;
    this.formActiveFlag = 'Y';
    this.dialogSubCategoryOptions = [];
    this.dialogVisible = true;
  }

  /**
   * 打開編輯彈窗
   */
  openEditDialog(item: SettingItem): void {
    this.isEditMode = true;
    this.editId = item.id;
    this.formTenant = item.tenant;
    this.formCategory = item.category;
    this.formSubCategory = item.subCategory || '';
    this.formCode = item.code;
    this.formName = item.name;
    this.formDescription = item.description || '';
    this.formValue = item.value;
    this.formPriorityNo = item.priorityNo;
    this.formActiveFlag = item.activeFlag;

    this.onDialogCategoryChange();
    this.dialogVisible = true;
  }

  /**
   * 儲存設定 (新增或修改)
   */
  saveSetting(): void {
    if (!this.formCategory || !this.formCode || !this.formName || !this.formValue) {
      this.messageService.add({
        severity: 'warn',
        summary: '驗證提醒',
        detail: '請完整填寫 Category, Code, Name 與 Value 欄位！'
      });
      return;
    }

    if (this.isEditMode && this.editId) {
      const updateReq: UpdateSettingRequest = {
        category: this.formCategory,
        subCategory: this.formSubCategory,
        name: this.formName,
        description: this.formDescription,
        value: this.formValue,
        priorityNo: this.formPriorityNo,
        activeFlag: this.formActiveFlag
      };

      this.settingService.update(this.editId, updateReq).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: '更新成功',
            detail: `設定 [${this.formCode}] 已成功更新`
          });
          this.dialogVisible = false;
          this.loadData();
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: '更新失敗',
            detail: err.error?.message || '更新失敗，請檢查參數'
          });
        }
      });
    } else {
      const createReq: CreateSettingRequest = {
        tenant: this.formTenant || this.currentTenant,
        category: this.formCategory,
        subCategory: this.formSubCategory,
        code: this.formCode,
        name: this.formName,
        description: this.formDescription,
        value: this.formValue,
        priorityNo: this.formPriorityNo
      };

      this.settingService.create(createReq).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: '建立成功',
            detail: `設定 [${this.formCode}] 已成功新增`
          });
          this.dialogVisible = false;
          this.loadData();
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: '新增失敗',
            detail: err.error?.message || '新增失敗，請檢查是否有重複唯一鍵 (tenant + category + code)'
          });
        }
      });
    }
  }

  /**
   * 刪除確認
   */
  confirmDelete(item: SettingItem): void {
    this.confirmationService.confirm({
      message: `確定要停用/刪除設定項目 [${item.name} (${item.code})] 嗎？`,
      header: '刪除確認',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: '確定刪除',
      rejectLabel: '取消',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.settingService.delete(item.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: '刪除成功',
              detail: `設定 [${item.code}] 已標記為停用`
            });
            this.loadData();
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: '刪除失敗',
              detail: '執行刪除時發生錯誤'
            });
          }
        });
      }
    });
  }
}
