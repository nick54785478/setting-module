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
 * 遵循 Clean Architecture 依賴反轉原則 (DIP)，僅依賴 SettingCategoryRepositoryPort，
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

    @Override
    @Transactional(readOnly = true)
    public List<CategoryGottenResult> getAll(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        return categoryRepositoryPort.findAllByTenant(resolvedTenant, YesNo.Y);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryGottenResult> getAllIncludeInactive(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        return categoryRepositoryPort.findAllByTenantIncludeInactive(resolvedTenant);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryGottenResult getById(Long id) {
        return categoryRepositoryPort.findByIdAndActiveFlag(id, YesNo.Y)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

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

    @Override
    public CategoryUpdatedResult update(UpdateCategoryCommand command) {
        if (command == null || command.id() == null) {
            throw new IllegalArgumentException("Category ID cannot be null.");
        }

        categoryRepositoryPort.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + command.id()));

        return categoryRepositoryPort.update(command);
    }

    @Override
    public void delete(Long id) {
        categoryRepositoryPort.deleteById(id);
    }

    private String resolveTenant(String tenant) {
        if (tenant != null && !tenant.isBlank()) {
            return tenant.trim();
        }
        String contextTenant = UserContextHolder.getTenant();
        return (contextTenant != null && !contextTenant.isBlank()) ? contextTenant : "CW";
    }
}
