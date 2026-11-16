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
import { OverlayPanelModule, OverlayPanel } from 'primeng/overlaypanel';
import { CheckboxModule } from 'primeng/checkbox';
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
import { CustomisationService } from '../../../../core/services/customisation.service';
import { CategoryService } from '../../services/category.service';
import { CategoryItem, CreateCategoryRequest, UpdateCategoryRequest } from '../../models/category.model';

/**
 * 元資料字典維護元件 (CategoryManagementComponent)
 * <p>
 * 專門負責全系統業務大維度 (Meta Category) 之新增、修改、停用與檢視。
 * 與業務設定 (Setting) 完全解耦。
 * </p>
 */
@Component({
  selector: 'app-category-management',
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
    OverlayPanelModule,
    CheckboxModule
  ],
  templateUrl: './category-management.component.html',
  styleUrl: './category-management.component.scss'
})
export class CategoryManagementComponent implements OnInit, OnDestroy {
  private tenantSub!: Subscription;
  currentTenant: string = 'CW';

  categories: CategoryItem[] = [];
  loading: boolean = false;

  // 對話框狀態
  dialogVisible: boolean = false;
  isEditMode: boolean = false;
  editId: number | null = null;

  // 表單資料
  formTenant: string = '';
  formCode: string = '';
  formName: string = '';
  formDescription: string = '';
  formPriorityNo: number = 1;
  formActiveFlag: 'Y' | 'N' = 'Y';

  // 個人化表格欄位顯示 (FieldView)
  readonly componentName = 'CategoryComponent';
  readonly defaultColumns: { field: string; label: string; visible: boolean }[] = [
    { field: 'priorityNo', label: '順序', visible: true },
    { field: 'tenant', label: '租戶識別', visible: true },
    { field: 'code', label: '維度代碼 (Code)', visible: true },
    { field: 'name', label: '維度名稱 (Name)', visible: true },
    { field: 'description', label: '維度說明 (Description)', visible: true },
    { field: 'activeFlag', label: '有效狀態', visible: true }
  ];
  columns: { field: string; label: string; visible: boolean }[] = [];
  draftColumns: { field: string; label: string; visible: boolean }[] = [];

  constructor(
    private categoryService: CategoryService,
    private tenantService: TenantService,
    private customisationService: CustomisationService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // 1. 先嘗試從本地快取瞬間初始化 (防重新整理閃爍)
    const localFields = this.customisationService.getLocalCustomisation(this.componentName, 'FieldView');
    if (localFields && localFields.length > 0) {
      const localSet = new Set(localFields);
      this.columns = this.defaultColumns.map((col) => ({
        ...col,
        visible: localSet.has(col.field)
      }));
    } else {
      // 尚未配置 -> 預設全顯
      this.columns = this.defaultColumns.map((col) => ({ ...col, visible: true }));
    }

    // 2. 向後端同步最新個人化配置 (若尚未配置則維持全顯，直到第一次儲存)
    this.loadColumnCustomisation();

    this.tenantSub = this.tenantService.tenant$.subscribe((tenant) => {
      this.currentTenant = tenant;
      this.loadData();
    });
  }

  ngOnDestroy(): void {
    if (this.tenantSub) {
      this.tenantSub.unsubscribe();
    }
  }

  loadData(): void {
    this.loading = true;
    this.categoryService.getCategories(this.currentTenant, true).subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: '載入失敗',
          detail: '無法取得元資料字典清單'
        });
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode = false;
    this.editId = null;
    this.formTenant = this.currentTenant;
    this.formCode = '';
    this.formName = '';
    this.formDescription = '';
    this.formPriorityNo = this.categories.length + 1;
    this.formActiveFlag = 'Y';
    this.dialogVisible = true;
  }

  openEditDialog(item: CategoryItem): void {
    this.isEditMode = true;
    this.editId = item.id;
    this.formTenant = item.tenant;
    this.formCode = item.code;
    this.formName = item.name;
    this.formDescription = item.description || '';
    this.formPriorityNo = item.priorityNo;
    this.formActiveFlag = item.activeFlag;
    this.dialogVisible = true;
  }

  saveCategory(): void {
    if (!this.formCode || !this.formName) {
      this.messageService.add({
        severity: 'warn',
        summary: '驗證提醒',
        detail: '維度唯一代碼 (Code) 與顯示名稱 (Name) 為必填項！'
      });
      return;
    }

    if (this.isEditMode && this.editId) {
      const updateReq: UpdateCategoryRequest = {
        name: this.formName,
        description: this.formDescription,
        priorityNo: this.formPriorityNo,
        activeFlag: this.formActiveFlag
      };

      this.categoryService.update(this.editId, updateReq).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: '更新成功',
            detail: `維度 [${this.formCode}] 已成功更新`
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
      const createReq: CreateCategoryRequest = {
        tenant: this.formTenant || this.currentTenant,
        code: this.formCode,
        name: this.formName,
        description: this.formDescription,
        priorityNo: this.formPriorityNo
      };

      this.categoryService.create(createReq).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: '建立成功',
            detail: `維度 [${this.formCode}] 已成功建立`
          });
          this.dialogVisible = false;
          this.loadData();
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: '建立失敗',
            detail: err.error?.message || '新增失敗，請檢查代碼是否已存在'
          });
        }
      });
    }
  }

  confirmDelete(item: CategoryItem): void {
    this.confirmationService.confirm({
      message: `確定要停用維度字典項目 [${item.name} (${item.code})] 嗎？`,
      header: '停用確認',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: '確定停用',
      rejectLabel: '取消',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.categoryService.delete(item.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: '停用成功',
              detail: `維度 [${item.code}] 已標記為停用`
            });
            this.loadData();
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: '停用失敗',
              detail: '執行停用操作時發生錯誤'
            });
          }
        });
      }
    });
  }

  /**
   * 載入使用者欄位個人化配置 (若查無紀錄自動 Fallback 預設全顯)
   */
  loadColumnCustomisation(): void {
    this.customisationService.getCustomisation(this.componentName, 'FieldView').subscribe({
      next: (fields) => {
        if (fields && fields.length > 0) {
          const visibleSet = new Set(fields);
          this.columns = this.defaultColumns.map((col) => ({
            ...col,
            visible: visibleSet.has(col.field)
          }));
        } else {
          // 後端尚未配置 -> 若無本地快取則顯示全部欄位 (不主動存入，等到使用者第一次點擊儲存才存)
          const local = this.customisationService.getLocalCustomisation(this.componentName, 'FieldView');
          if (!local) {
            this.columns = this.defaultColumns.map((col) => ({ ...col, visible: true }));
          }
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.warn('載入維度欄位自訂設定失敗，採用目前佈局', err);
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * 開啟欄位自訂浮動面板，建立暫存 Draft 狀態
   */
  openColumnCustomisation(event: Event, op: OverlayPanel): void {
    this.draftColumns = this.columns.map((c) => ({ ...c }));
    op.toggle(event);
  }

  /**
   * 提交並持久化儲存欄位自訂配置 (傳遞 field 陣列，後端以逗號分隔儲存)
   */
  saveColumnCustomisation(op: OverlayPanel): void {
    const visibleFields: string[] = this.draftColumns
      .filter((c) => c.visible)
      .map((c) => c.field);

    this.customisationService.saveCustomisation(this.componentName, visibleFields, 'FieldView').subscribe({
      next: () => {
        this.columns = this.draftColumns.map((c) => ({ ...c }));
        op.hide();
        this.cdr.markForCheck();
        this.messageService.add({
          severity: 'success',
          summary: '儲存成功',
          detail: '維度表格欄位顯示偏好已儲存並生效',
          life: 3000
        });
      },
      error: (err) => {
        console.error('儲存維度欄位配置失敗', err);
        this.messageService.add({
          severity: 'error',
          summary: '儲存失敗',
          detail: '儲存欄位自訂設定失敗'
        });
      }
    });
  }

  /**
   * 取消並拋棄暫存 Draft 狀態
   */
  cancelColumnCustomisation(op: OverlayPanel): void {
    op.hide();
  }

  /**
   * 檢查特定欄位當前是否顯示
   */
  isColumnVisible(field: string): boolean {
    const col = this.columns.find((c) => c.field === field);
    return col ? col.visible : true;
  }
}
