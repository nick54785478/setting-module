package com.module.setting.application.port.in;

/**
 * 邏輯刪除維度元資料用例 (DeleteCategoryUseCase)
 */
public interface DeleteCategoryUseCase {

    /**
     * 依據主鍵 ID 邏輯刪除維度元資料
     *
     * @param id 維度主鍵 ID
     */
    void delete(Long id);
}
