export interface FormFieldOption {
  label: string;
  value: any;
}

export interface FormField {
  name: string;
  type: 'text' | 'number' | 'email' | 'time' | 'dropdown' | 'calendar' | 'autocomplete' | 'textarea' | string;
  label: string;
  validators: string[];
  options?: FormFieldOption[]; // 適用於 dropdown
  pattern?: string;            // 適用於 calendar (例如 'yy-mm-dd')
  placeholder?: string;
  rows?: number | string;      // 適用於 textarea 列數
  cols?: number | string;      // 適用於 textarea 行數
  uri?: string;                // 適用於 autocomplete 資料來源標識
}
