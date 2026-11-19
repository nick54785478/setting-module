package com.module.setting.application.port.out;

import com.module.setting.application.shared.command.CreateCategoryCommand;
import com.module.setting.application.shared.command.UpdateCategoryCommand;
import com.module.setting.application.shared.result.CategoryCreatedResult;
import com.module.setting.application.shared.result.CategoryGottenResult;
import com.module.setting.application.shared.result.CategoryUpdatedResult;
import com.module.setting.application.shared.vo.YesNo;

import java.util.List;
import java.util.Optional;

/**
 * 維度元資料倉儲輸出埠 (SettingCategoryRepositoryPort)
 * <p>
 * 應用層 Outbound Port，遵循依賴反轉原則 (DIP) 與 Clean Architecture 規範，
 * 嚴禁使用 Infrastructure 層之 ORM Entity (SettingCategory) 或技術類別，
 * 僅透過應用層介面、Command 與 Result DTO 與持久化層進行互動。
 * </p>
 */
public interface SettingCategoryRepositoryPort {

    /**
     * 依據租戶與有效旗標查詢所有維度字典項目
     *
     * @param tenant     租戶識別碼
     * @param activeFlag 有效狀態旗標
     * @return 維度字典結果清單
     */
    List<CategoryGottenResult> findAllByTenant(String tenant, YesNo activeFlag);

    /**
     * 依據租戶查詢所有維度字典項目 (包含已停用項目)
     *
     * @param tenant 租戶識別碼
     * @return 維度字典結果清單
     */
    List<CategoryGottenResult> findAllByTenantIncludeInactive(String tenant);

    /**
     * 依據主鍵 ID 與有效旗標查詢單一維度字典項目
     *
     * @param id         維度主鍵 ID
     * @param activeFlag 有效狀態旗標
     * @return 匹配之維度字典結果 Optional
     */
    Optional<CategoryGottenResult> findByIdAndActiveFlag(Long id, YesNo activeFlag);

    /**
     * 依據主鍵 ID 查詢單一維度字典項目 (無論是否有效)
     *
     * @param id 維度主鍵 ID
     * @return 匹配之維度字典結果 Optional
     */
    Optional<CategoryGottenResult> findById(Long id);

    /**
     * 檢查指定租戶與代碼是否已存在維度字典紀錄
     *
     * @param tenant 租戶識別碼
     * @param code   維度唯一代碼
     * @return 若存在回傳 true，否則回傳 false
     */
    boolean existsByTenantAndCode(String tenant, String code);

    /**
     * 新增維度字典資料持久化
     *
     * @param command 建立維度字典指令
     * @return 建立成功後之維度結果 DTO
     */
    CategoryCreatedResult save(CreateCategoryCommand command);

    /**
     * 更新維度字典資料持久化
     *
     * @param command 更新維度字典指令
     * @return 更新成功後之維度結果 DTO
     */
    CategoryUpdatedResult update(UpdateCategoryCommand command);

    /**
     * 軟刪除/停用指定 ID 之維度字典資料
     *
     * @param id 維度主鍵 ID
     */
    void deleteById(Long id);
}
