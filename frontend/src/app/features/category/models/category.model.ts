/**
 * 維度元資料領域模型 (CategoryItem)
 */
export interface CategoryItem {
  id: number;
  tenant: string;
  code: string;
  name: string;
  description?: string;
  priorityNo: number;
  activeFlag: 'Y' | 'N';
  label: string;
  value: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 建立維度請求模型 (CreateCategoryRequest)
 */
export interface CreateCategoryRequest {
  tenant?: string;
  code: string;
  name: string;
  description?: string;
  priorityNo: number;
}

/**
 * 更新維度請求模型 (UpdateCategoryRequest)
 */
export interface UpdateCategoryRequest {
  name: string;
  description?: string;
  priorityNo: number;
  activeFlag: 'Y' | 'N';
}
