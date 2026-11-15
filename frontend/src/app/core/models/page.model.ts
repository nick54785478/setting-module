/**
 * 全域基礎設施分頁回應泛型模型 (PagedResult)
 */
export interface PagedResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
