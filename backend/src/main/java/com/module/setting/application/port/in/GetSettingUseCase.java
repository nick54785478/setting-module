package com.module.setting.application.port.in;

import com.module.setting.application.shared.result.SettingGottenResult;
import java.util.List;

/**
 * 查詢設定用例介面 (GetSettingUseCase)
 * <p>
 * 應用層 Inbound Port，定義多維度查詢設定資訊的業務合約。
 * </p>
 */
public interface GetSettingUseCase {

    /**
     * 依據主鍵 ID 查詢設定
     *
     * @param id 設定 ID
     * @return 設定結果 DTO
     */
    SettingGottenResult getById(Long id);

    /**
     * 依據租戶代碼與設定唯一代碼查詢單一設定
     *
     * @param tenant 租戶代碼
     * @param code   設定代碼
     * @return 設定結果 DTO
     */
    SettingGottenResult getByScope(String tenant, String code);

    /**
     * 依據租戶代碼與大分類 (Category) 查詢設定清單 (依 priorityNo 升冪排序)
     *
     * @param tenant   租戶代碼
     * @param category 功能大維度 (ROLE, GROUP, FUNCTION 等)
     * @return 設定結果 DTO 清單
     */
    List<SettingGottenResult> getByCategory(String tenant, String category);

    /**
     * 依據租戶代碼、大分類與子分類 (SubCategory) 查詢設定清單 (依 priorityNo 升冪排序)
     *
     * @param tenant      租戶代碼
     * @param category    功能大維度
     * @param subCategory 業務子分類 (DASHBOARD, USER, ADMIN 等)
     * @return 設定結果 DTO 清單
     */
    List<SettingGottenResult> getByCategoryAndSubCategory(String tenant, String category, String subCategory);

    /**
     * 依據租戶代碼查詢所有有效設定
     *
     * @param tenant 租戶代碼
     * @return 設定結果 DTO 清單
     */
    List<SettingGottenResult> getAllByTenant(String tenant);
}
