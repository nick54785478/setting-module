package com.module.setting.application.port.out;

import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingCreatedResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;

import java.util.List;
import java.util.Optional;

/**
 * 設定倉儲輸出埠介面 (SettingRepositoryPort)
 * <p>
 * 應用層 Outbound Port，嚴禁使用任何技術類或 ORM 實體物件作為參數或回傳值，
 * 僅接收 Command、Query 並回傳 DTO (SettingCreatedResult, SettingUpdatedResult, SettingGottenResult, PagedDataGottenResult, SettingOptionGottenResult)。
 * </p>
 */
public interface SettingRepositoryPort {

    /**
     * 新增設定持久化
     *
     * @param command 建立設定指令
     * @return 新增後的設定結果 DTO
     */
    SettingCreatedResult save(CreateSettingCommand command);

    /**
     * 更新設定持久化
     *
     * @param command 更新設定指令
     * @return 更新後的設定結果 DTO
     */
    SettingUpdatedResult update(UpdateSettingCommand command);

    /**
     * 檢查指定租戶、大分類與代碼之設定是否存在
     *
     * @param tenant   租戶代碼
     * @param category 業務功能維度
     * @param code     設定代碼
     * @return 若已存在回傳 true，否則回傳 false
     */
    boolean exists(String tenant, String category, String code);

    /**
     * 依主鍵 ID 查詢有效設定
     *
     * @param id 設定 ID
     * @return 符合條件的設定結果 DTO 可選物件
     */
    Optional<SettingGottenResult> findById(Long id);

    /**
     * 依租戶代碼與設定唯一識別碼查詢有效設定
     *
     * @param tenant 租戶代碼
     * @param code   設定代碼
     * @return 符合條件的設定結果 DTO 可選物件
     */
    Optional<SettingGottenResult> findByScope(String tenant, String code);

    /**
     * 依租戶代碼與業務功能大分類查詢有效設定清單 (依 priorityNo 升冪排序)
     *
     * @param tenant   租戶代碼
     * @param category 業務功能維度 (例如: FUNCTION, ROLE)
     * @return 設定結果 DTO 清單
     */
    List<SettingGottenResult> findByCategory(String tenant, String category);

    /**
     * 依租戶代碼、大分類與子分類查詢有效設定清單 (依 priorityNo 升冪排序)
     *
     * @param tenant      租戶代碼
     * @param category    業務功能維度
     * @param subCategory 業務子分類
     * @return 設定結果 DTO 清單
     */
    List<SettingGottenResult> findByCategoryAndSubCategory(String tenant, String category, String subCategory);

    /**
     * 依租戶代碼查詢有效設定清單
     *
     * @param tenant 租戶代碼
     * @return 設定結果 DTO 清單
     */
    List<SettingGottenResult> findByTenant(String tenant);

    /**
     * 依多維條件動態分頁查詢有效設定
     *
     * @param query 分頁查詢參數指令
     * @return 包含 SettingGottenResult 的通用分頁結果
     */
    PagedDataGottenResult<SettingGottenResult> findPaged(GetSettingPagedQuery query);

    /**
     * 取得所有業務功能大分類 (Category) 下拉選單選項清單
     *
     * @param tenant 租戶代碼 (可選)
     * @return Category 選項清單 (name = label, code = value)
     */
    List<SettingOptionGottenResult> findCategoryOptions(String tenant);

    /**
     * 依據指定的 Category 取得相關的 SubCategory 下拉選單選項清單
     *
     * @param tenant   租戶代碼 (可選)
     * @param category 業務功能大分類
     * @return SubCategory 選項清單 (name = label, code = value)
     */
    List<SettingOptionGottenResult> findSubCategoryOptions(String tenant, String category);

    /**
     * 依主鍵 ID 邏輯刪除設定
     *
     * @param id 設定 ID
     */
    void deleteById(Long id);

    /**
     * 批次更新設定項目排序順序號，並回傳依 priorityNo 升冪排序之清單
     *
     * @param commands 排序指令清單
     * @return 排序更新後之設定結果清單 (嚴格依 priorityNo 升冪排序)
     */
    List<SettingGottenResult> reorder(List<com.module.setting.application.shared.command.ReorderSettingItemCommand> commands);
}
