package com.module.setting.application.service;

import com.module.setting.application.port.in.*;
import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.command.DeleteSettingCommand;
import com.module.setting.application.shared.command.ReorderSettingItemCommand;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingCreatedResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import com.module.setting.application.shared.template.SettingSeedTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 設定應用層服務 (SettingApplicationService)
 * <p>
 * 實作所有 Inbound Ports，調度 Outbound Port ({@link SettingRepositoryPort})
 * 進行業務流程協調、多租戶隔離檢核、事務控制與資料初始化。
 * 遵循 Clean Architecture 架構，絕不直接依賴 Infrastructure 持久化實作。
 * </p>
 */
@Service
@Transactional
public class SettingApplicationService implements
        CreateSettingUseCase,
        UpdateSettingUseCase,
        GetSettingUseCase,
        GetSettingPagedUseCase,
        GetSettingOptionUseCase,
        InitTenantSettingUseCase,
        DeleteSettingUseCase,
        ReorderSettingUseCase {

    private final SettingRepositoryPort settingRepositoryPort;

    /**
     * 建構子注入設定倉儲輸出埠
     *
     * @param settingRepositoryPort 設定倉儲輸出埠
     */
    public SettingApplicationService(SettingRepositoryPort settingRepositoryPort) {
        this.settingRepositoryPort = settingRepositoryPort;
    }

    /**
     * 新增業務設定項目
     * <p>
     * 接收前端建立指令，委託倉儲輸出埠進行多租戶唯一性檢核與持久化儲存。
     * </p>
     *
     * @param command 建立設定指令 DTO
     * @return 新增完成後之設定結果 DTO
     */
    @Override
    public SettingCreatedResult create(CreateSettingCommand command) {
        return settingRepositoryPort.save(command);
    }

    /**
     * 更新既有業務設定項目
     * <p>
     * 更新前執行防呆檢核：若該項目屬於系統語系 (LOCALE) 且當前為預設值 (DEFAULT)，
     * 則禁止將其停用 (activeFlag = N)，避免系統陷入無有效預設語言之例外狀態。
     * </p>
     *
     * @param command 更新設定指令 DTO
     * @return 更新完成後之設定結果 DTO
     * @throws IllegalArgumentException 當嘗試停用使用中之預設語系時拋出
     */
    @Override
    public SettingUpdatedResult update(UpdateSettingCommand command) {
        validateLocaleNotDeactivatedIfInUse(command);
        return settingRepositoryPort.update(command);
    }

    /**
     * 依據主鍵 ID 查詢單筆設定資料
     *
     * @param id 設定項目主鍵 ID
     * @return 匹配之設定結果 DTO
     * @throws IllegalArgumentException 當查無該 ID 之設定時拋出
     */
    @Override
    @Transactional(readOnly = true)
    public SettingGottenResult getById(Long id) {
        return settingRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with id: " + id));
    }

    /**
     * 依據租戶與設定唯一代碼查詢單筆設定資料
     *
     * @param tenant 租戶識別碼
     * @param code   設定唯一代碼
     * @return 匹配之設定結果 DTO
     * @throws IllegalArgumentException 當查無符合該坐標之設定時拋出
     */
    @Override
    @Transactional(readOnly = true)
    public SettingGottenResult getByScope(String tenant, String code) {
        return settingRepositoryPort.findByScope(tenant, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Setting not found with tenant: " + tenant + ", code: " + code));
    }

    /**
     * 依據租戶與大業務分類 (Category) 查詢所有有效設定清單
     *
     * @param tenant   租戶識別碼
     * @param category 業務功能大分類 (如 LOCALE, MENU, SYSTEM)
     * @return 該分類下之設定結果 DTO 清單
     */
    @Override
    @Transactional(readOnly = true)
    public List<SettingGottenResult> getByCategory(String tenant, String category) {
        return settingRepositoryPort.findByCategory(tenant, category);
    }

    /**
     * 依據租戶、大業務分類與子分類查詢設定清單
     *
     * @param tenant      租戶識別碼
     * @param category    業務功能大分類
     * @param subCategory 子分類或功能模組
     * @return 符合二級分類之設定結果 DTO 清單
     */
    @Override
    @Transactional(readOnly = true)
    public List<SettingGottenResult> getByCategoryAndSubCategory(String tenant, String category, String subCategory) {
        return settingRepositoryPort.findByCategoryAndSubCategory(tenant, category, subCategory);
    }

    /**
     * 查詢指定租戶下所有已啟用之設定項目清單
     *
     * @param tenant 租戶識別碼
     * @return 該租戶所有設定結果 DTO 清單
     */
    @Override
    @Transactional(readOnly = true)
    public List<SettingGottenResult> getAllByTenant(String tenant) {
        return settingRepositoryPort.findByTenant(tenant);
    }

    /**
     * 依據分頁與多條件篩選查詢設定清單
     * <p>
     * 支援租戶、Category、SubCategory、Code 精準匹配、名稱模糊搜尋及啟用狀態過濾。
     * </p>
     *
     * @param query 分頁查詢條件封裝物件
     * @return 包含資料清單、總筆數、當前頁碼與每頁大小之分頁結果 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public PagedDataGottenResult<SettingGottenResult> getPaged(GetSettingPagedQuery query) {
        return settingRepositoryPort.findPaged(query);
    }

    /**
     * 取得指定租戶下可用之大業務分類 (Category) 下拉選項清單
     *
     * @param tenant 租戶識別碼
     * @return 大分類下拉選項 DTO 清單
     */
    @Override
    @Transactional(readOnly = true)
    public List<SettingOptionGottenResult> getCategoryOptions(String tenant) {
        return settingRepositoryPort.findCategoryOptions(tenant);
    }

    /**
     * 依據指定租戶與大分類，取得所屬之子模組 (SubCategory) 下拉選項清單
     *
     * @param tenant   租戶識別碼
     * @param category 業務功能大分類
     * @return 子分類下拉選項 DTO 清單
     * @throws IllegalArgumentException 當 category 為空白時拋出
     */
    @Override
    @Transactional(readOnly = true)
    public List<SettingOptionGottenResult> getSubCategoryOptions(String tenant, String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank when retrieving sub-category options.");
        }
        return settingRepositoryPort.findSubCategoryOptions(tenant, category);
    }

    /**
     * 初始化指定租戶之基準預設功能設定 (Tenant Onboarding / Seeding)
     * <p>
     * 具備冪等性 (Idempotent) 防護：遍歷預設樣板清單，若該租戶已存在相同 category 與 code 的設定項目，
     * 則自動跳過，不重複建立亦不覆蓋已有資料，安全支援多次呼叫。
     * </p>
     *
     * @param tenant 欲初始化的目標租戶代碼
     * @return 實際新增之設定筆數
     * @throws IllegalArgumentException 當 tenant 為空白時拋出
     */
    @Override
    public int initDefaultSettings(String tenant) {
        if (tenant == null || tenant.isBlank()) {
            throw new IllegalArgumentException("Tenant cannot be blank during initialization.");
        }

        List<CreateSettingCommand> templates = SettingSeedTemplate.getDefaultTemplates(tenant.trim());
        int createdCount = 0;

        for (CreateSettingCommand template : templates) {
            // 冪等性防護：若該租戶已存在相同 category 與 code 的設定項目，則自動跳過，不重複建立亦不覆蓋
            if (!settingRepositoryPort.exists(template.tenant(), template.category(), template.code())) {
                settingRepositoryPort.save(template);
                createdCount++;
            }
        }

        return createdCount;
    }

    /**
     * 刪除業務設定項目
     * <p>
     * 刪除前執行關鍵資源保護檢核：若該項目屬於系統預設語系 (LOCALE 且 value=DEFAULT)，
     * 則強制阻擋刪除，維護多語系運作穩定。
     * </p>
     *
     * @param command 刪除設定指令 DTO
     * @throws IllegalArgumentException 當嘗試刪除使用中之預設語系時拋出
     */
    @Override
    public void delete(DeleteSettingCommand command) {
        validateLocaleNotDeletedIfInUse(command.id());
        settingRepositoryPort.deleteById(command.id());
    }

    /**
     * 批次更新設定項目之顯示優先順序 (Priority Reordering)
     * <p>
     * 用於支援前端表格拖曳排序，整批更新指定設定項目之 priorityNo。
     * </p>
     *
     * @param commands 拖曳排序指令清單 (包含各項目之 id 與新 priorityNo)
     * @return 更新排序後之設定結果清單
     */
    @Override
    public List<SettingGottenResult> reorder(List<ReorderSettingItemCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        return settingRepositoryPort.reorder(commands);
    }

    /**
     * 檢核語系設定項目於停用時是否為預設首選 (DEFAULT)
     * <p>
     * 若該設定屬於 LOCALE 類別且其 value 為 "DEFAULT"，若嘗試將其停用則拋出業務檢核例外。
     * </p>
     *
     * @param command 更新指令 DTO
     */
    private void validateLocaleNotDeactivatedIfInUse(UpdateSettingCommand command) {
        settingRepositoryPort.findById(command.id()).ifPresent(setting -> {
            boolean isLocale = "LOCALE".equalsIgnoreCase(setting.category())
                    || (command.category() != null && "LOCALE".equalsIgnoreCase(command.category()));
            if (isLocale) {
                boolean isDefault = "DEFAULT".equalsIgnoreCase(setting.value());
                boolean willDeactivate = "N".equalsIgnoreCase(command.activeFlag());
                if (isDefault && willDeactivate) {
                    throw new IllegalArgumentException("租戶預設語系 (DEFAULT) 正在使用中，不得停用。請先指派其他有效語系為預設後再執行停用。");
                }
            }
        });
    }

    /**
     * 檢核語系設定項目於刪除時是否為預設首選 (DEFAULT)
     * <p>
     * 若該設定屬於 LOCALE 類別且其 value 為 "DEFAULT"，若嘗試將其刪除則拋出業務檢核例外。
     * </p>
     *
     * @param id 設定項目主鍵 ID
     */
    private void validateLocaleNotDeletedIfInUse(Long id) {
        settingRepositoryPort.findById(id).ifPresent(setting -> {
            if ("LOCALE".equalsIgnoreCase(setting.category()) && "DEFAULT".equalsIgnoreCase(setting.value())) {
                throw new IllegalArgumentException("租戶預設語系 (DEFAULT) 正在使用中，不得刪除。請先指派其他有效語系為預設後再執行刪除。");
            }
        });
    }
}
