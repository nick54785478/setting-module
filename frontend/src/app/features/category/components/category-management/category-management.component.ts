import { Component, OnDestroy, OnInit } from '@angular/core';
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
import { ConfirmationService, MessageService } from 'primeng/api';

import { TenantService } from '../../../../core/services/tenant.service';
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
    TooltipModule
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

  constructor(
    private categoryService: CategoryService,
    private tenantService: TenantService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
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
}
