package com.module.setting.application.port.in;

import com.module.setting.application.shared.result.SettingOptionGottenResult;
import java.util.List;

/**
 * 查詢設定下拉選單選項用例介面 (GetSettingOptionUseCase)
 * <p>
 * 應用層 Inbound Port，定義為前端下拉選單提供 Category 與 SubCategory 選項資料之業務合約。
 * </p>
 */
public interface GetSettingOptionUseCase {

    /**
     * 取得所有業務功能大分類 (Category) 下拉選項
     *
     * @param tenant 租戶代碼 (可選)
     * @return Category 選項清單 (name = option.label, code = option.value)
     */
    List<SettingOptionGottenResult> getCategoryOptions(String tenant);

    /**
     * 依據指定的 Category 取得相關的 SubCategory 下拉選項
     *
     * @param tenant   租戶代碼 (可選)
     * @param category 業務功能大分類 (例如: FUNCTION, ROLE, GROUP)
     * @return SubCategory 選項清單 (name = option.label, code = option.value)
     */
    List<SettingOptionGottenResult> getSubCategoryOptions(String tenant, String category);
}
