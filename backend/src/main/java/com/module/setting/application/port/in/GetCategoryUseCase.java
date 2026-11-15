package com.module.setting.application.port.in;

import com.module.setting.application.shared.result.CategoryGottenResult;
import java.util.List;

/**
 * 查詢維度元資料用例 (GetCategoryUseCase)
 */
public interface GetCategoryUseCase {

    /**
     * 依據租戶取得所有有效維度元資料清單
     *
     * @param tenant 租戶代碼 (可選)
     * @return 維度清單 (依順序號升冪排序)
     */
    List<CategoryGottenResult> getAll(String tenant);

    /**
     * 依據租戶取得所有維度 (包含已停用，供字典管理後台展示)
     *
     * @param tenant 租戶代碼 (可選)
     * @return 完整維度清單
     */
    List<CategoryGottenResult> getAllIncludeInactive(String tenant);

    /**
     * 依據主鍵 ID 查詢單一維度
     *
     * @param id 維度主鍵 ID
     * @return 維度結果
     */
    CategoryGottenResult getById(Long id);
}
