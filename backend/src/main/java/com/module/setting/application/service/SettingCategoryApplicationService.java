package com.module.setting.application.service;

import com.module.setting.application.port.in.CreateCategoryUseCase;
import com.module.setting.application.port.in.DeleteCategoryUseCase;
import com.module.setting.application.port.in.GetCategoryUseCase;
import com.module.setting.application.port.in.UpdateCategoryUseCase;
import com.module.setting.application.port.out.SettingCategoryRepositoryPort;
import com.module.setting.application.shared.command.CreateCategoryCommand;
import com.module.setting.application.shared.command.UpdateCategoryCommand;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.result.CategoryCreatedResult;
import com.module.setting.application.shared.result.CategoryGottenResult;
import com.module.setting.application.shared.result.CategoryUpdatedResult;
import com.module.setting.application.shared.vo.YesNo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 維度元資料領域應用服務 (SettingCategoryApplicationService)
 * <p>
 * 實作維度字典之查詢、新增、修改與刪除業務合約。
 * 遵循 Clean Architecture 依賴反轉原則 (DIP)，僅依賴 {@link SettingCategoryRepositoryPort}，
 * 嚴禁直接引用 Infrastructure 層之 JPA Entity 或技術實作。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SettingCategoryApplicationService implements
        GetCategoryUseCase,
        CreateCategoryUseCase,
        UpdateCategoryUseCase,
        DeleteCategoryUseCase {

    private final SettingCategoryRepositoryPort categoryRepositoryPort;

    /**
     * 取得指定租戶下所有已啟用 (activeFlag = Y) 之維度字典項目清單
     * <p>
     * 當未指定租戶時，自動由 {@link UserContextHolder} 取得當前使用者所屬租戶。
     * </p>
     *
     * @param tenant 租戶識別碼 (可為空，為空時自動解析上下文)
     * @return 該租戶之有效維度字典 DTO 清單
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryGottenResult> getAll(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        return categoryRepositoryPort.findAllByTenant(resolvedTenant, YesNo.Y);
    }

    /**
     * 取得指定租戶下所有維度字典項目清單 (包含有效與已停用項目)
     * <p>
     * 提供予維護管理後台進行完整元資料總覽與狀態維護使用。
     * </p>
     *
     * @param tenant 租戶識別碼 (可為空，為空時自動解析上下文)
     * @return 該租戶之所有維度字典 DTO 清單 (含有效與無效)
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryGottenResult> getAllIncludeInactive(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        return categoryRepositoryPort.findAllByTenantIncludeInactive(resolvedTenant);
    }

    /**
     * 依據主鍵 ID 查詢指定有效之維度字典項目
     *
     * @param id 維度主鍵 ID
     * @return 匹配之維度字典 DTO 結果
     * @throws IllegalArgumentException 當查無該 ID 或該項目已停用時拋出
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryGottenResult getById(Long id) {
        return categoryRepositoryPort.findByIdAndActiveFlag(id, YesNo.Y)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

    /**
     * 建立新的維度元資料字典項目
     * <p>
     * 檢核維度代碼 (code) 與名稱 (name) 不得為空，且該租戶下維度代碼必須具備唯一性。
     * 檢核通過後委派倉儲輸出埠完成持久化。
     * </p>
     *
     * @param command 建立維度字典指令
     * @return 建立成功後之維度結果 DTO
     * @throws IllegalArgumentException 當代碼或名稱為空，或該租戶已存在相同代碼時拋出
     */
    @Override
    public CategoryCreatedResult create(CreateCategoryCommand command) {
        if (command == null || command.code() == null || command.code().isBlank() ||
                command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Category code and name cannot be blank.");
        }

        String tenant = resolveTenant(command.tenant());
        if (categoryRepositoryPort.existsByTenantAndCode(tenant, command.code().trim())) {
            throw new IllegalArgumentException("Category code already exists for tenant: " + tenant + ", code: " + command.code());
        }

        CreateCategoryCommand normalizedCommand = new CreateCategoryCommand(
                tenant,
                command.code().trim(),
                command.name().trim(),
                command.description(),
                command.priorityNo()
        );

        return categoryRepositoryPort.save(normalizedCommand);
    }

    /**
     * 更新既有維度元資料字典項目
     * <p>
     * 檢查該 ID 之維度紀錄是否存在，確認存在後委託倉儲輸出埠更新名稱、說明、優先序與有效旗標。
     * </p>
     *
     * @param command 更新維度字典指令
     * @return 更新成功後之維度結果 DTO
     * @throws IllegalArgumentException 當 command 為空或指定 ID 之資料不存在時拋出
     */
    @Override
    public CategoryUpdatedResult update(UpdateCategoryCommand command) {
        if (command == null || command.id() == null) {
            throw new IllegalArgumentException("Category ID cannot be null.");
        }

        categoryRepositoryPort.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + command.id()));

        return categoryRepositoryPort.update(command);
    }

    /**
     * 軟刪除/停用指定 ID 之維度字典項目
     *
     * @param id 維度主鍵 ID
     */
    @Override
    public void delete(Long id) {
        categoryRepositoryPort.deleteById(id);
    }

    /**
     * 解析租戶代碼輔助方法
     * <p>
     * 優先使用顯式傳入的租戶代碼，若為空則嘗試從 {@link UserContextHolder} 取得，最終回退為預設租戶 "CW"。
     * </p>
     *
     * @param tenant 外部傳入之租戶代碼
     * @return 確保非空之租戶識別碼
     */
    private String resolveTenant(String tenant) {
        if (tenant != null && !tenant.isBlank()) {
            return tenant.trim();
        }
        String contextTenant = UserContextHolder.getTenant();
        return (contextTenant != null && !contextTenant.isBlank()) ? contextTenant : "CW";
    }
}
