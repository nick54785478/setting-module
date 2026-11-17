package com.module.setting.application.service;

import com.module.setting.application.port.in.CreateCategoryUseCase;
import com.module.setting.application.port.in.DeleteCategoryUseCase;
import com.module.setting.application.port.in.GetCategoryUseCase;
import com.module.setting.application.port.in.UpdateCategoryUseCase;
import com.module.setting.application.shared.command.CreateCategoryCommand;
import com.module.setting.application.shared.command.UpdateCategoryCommand;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.result.CategoryCreatedResult;
import com.module.setting.application.shared.result.CategoryGottenResult;
import com.module.setting.application.shared.result.CategoryUpdatedResult;
import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.category.entity.SettingCategory;
import com.module.setting.infrastructure.persistence.category.repository.SettingCategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 維度元資料領域應用服務 (SettingCategoryApplicationService)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SettingCategoryApplicationService implements
        GetCategoryUseCase,
        CreateCategoryUseCase,
        UpdateCategoryUseCase,
        DeleteCategoryUseCase {

    private final SettingCategoryJpaRepository categoryJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryGottenResult> getAll(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        return categoryJpaRepository.findAllByTenant(resolvedTenant, YesNo.Y).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryGottenResult> getAllIncludeInactive(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        return categoryJpaRepository.findAllByTenantIncludeInactive(resolvedTenant).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryGottenResult getById(Long id) {
        return categoryJpaRepository.findByIdAndActiveFlag(id, YesNo.Y)
                .map(this::toResult)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

    @Override
    public CategoryCreatedResult create(CreateCategoryCommand command) {
        if (command == null || command.code() == null || command.code().isBlank() ||
                command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Category code and name cannot be blank.");
        }

        String tenant = resolveTenant(command.tenant());
        if (categoryJpaRepository.existsByTenantAndCode(tenant, command.code().trim())) {
            throw new IllegalArgumentException("Category code already exists for tenant: " + tenant + ", code: " + command.code());
        }

        SettingCategory category = SettingCategory.of(
                tenant,
                command.code().trim(),
                command.name().trim(),
                command.description(),
                command.priorityNo()
        );

        SettingCategory saved = categoryJpaRepository.save(category);
        return toCreatedResult(saved);
    }

    @Override
    public CategoryUpdatedResult update(UpdateCategoryCommand command) {
        if (command == null || command.id() == null) {
            throw new IllegalArgumentException("Category ID cannot be null.");
        }

        SettingCategory category = categoryJpaRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + command.id()));

        category.update(
                command.name(),
                command.description(),
                command.priorityNo(),
                command.activeFlag()
        );

        SettingCategory updated = categoryJpaRepository.save(category);
        return toUpdatedResult(updated);
    }

    @Override
    public void delete(Long id) {
        categoryJpaRepository.findById(id).ifPresent(category -> {
            category.delete();
            categoryJpaRepository.save(category);
        });
    }

    private String resolveTenant(String tenant) {
        if (tenant != null && !tenant.isBlank()) {
            return tenant.trim();
        }
        String contextTenant = UserContextHolder.getTenant();
        return (contextTenant != null && !contextTenant.isBlank()) ? contextTenant : "CW";
    }

    private CategoryGottenResult toResult(SettingCategory entity) {
        return CategoryGottenResult.of(
                entity.getId(),
                entity.getTenant(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getPriorityNo(),
                entity.getActiveFlag(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CategoryCreatedResult toCreatedResult(SettingCategory entity) {
        return CategoryCreatedResult.of(
                entity.getId(),
                entity.getTenant(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getPriorityNo(),
                entity.getActiveFlag(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CategoryUpdatedResult toUpdatedResult(SettingCategory entity) {
        return CategoryUpdatedResult.of(
                entity.getId(),
                entity.getTenant(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getPriorityNo(),
                entity.getActiveFlag(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
