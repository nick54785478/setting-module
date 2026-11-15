/**
 * 設定項目領域模型 (SettingItem)
 */
export interface SettingItem {
  id: number;
  tenant: string;
  category: string;
  subCategory?: string;
  code: string;
  name: string;
  description?: string;
  value: string;
  priorityNo: number;
  activeFlag: 'Y' | 'N';
}

/**
 * 建立設定請求命令模型 (CreateSettingRequest)
 */
export interface CreateSettingRequest {
  tenant?: string;
  category: string;
  subCategory?: string;
  code: string;
  name: string;
  description?: string;
  value: string;
  priorityNo: number;
}

/**
 * 更新設定請求命令模型 (UpdateSettingRequest)
 */
export interface UpdateSettingRequest {
  category?: string;
  subCategory?: string;
  name: string;
  description?: string;
  value: string;
  priorityNo: number;
  activeFlag: string;
}

/**
 * 元資料選項模型 (SettingOption)
 */
export interface SettingOption {
  code: string;
  name: string;
  label: string;
  value: string;
}

/**
 * 設定動態分頁查詢參數模型 (SettingQueryParams)
 */
export interface SettingQueryParams {
  tenant?: string;
  category?: string;
  subCategory?: string;
  code?: string;
  name?: string;
  activeFlag?: string;
  page: number;
  size: number;
}
