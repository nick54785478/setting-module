package com.module.setting.infrastructure.adapter;

import com.module.setting.application.port.out.SettingCategoryRepositoryPort;
import com.module.setting.application.shared.command.CreateCategoryCommand;
import com.module.setting.application.shared.command.UpdateCategoryCommand;
import com.module.setting.application.shared.result.CategoryCreatedResult;
import com.module.setting.application.shared.result.CategoryGottenResult;
import com.module.setting.application.shared.result.CategoryUpdatedResult;
import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.category.entity.SettingCategory;
import com.module.setting.infrastructure.persistence.category.repository.SettingCategoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 維度元資料倉儲適配器 (SettingCategoryRepositoryAdapter)
 * <p>
 * 實作應用層 Outbound Port (SettingCategoryRepositoryPort)，
 * 負責將應用層 Command/Result 與 Infrastructure JPA Entity (SettingCategory) 進行轉換與資料庫存取。
 * </p>
 */
@Component
public class SettingCategoryRepositoryAdapter implements SettingCategoryRepositoryPort {

    private final SettingCategoryJpaRepository categoryJpaRepository;

    public SettingCategoryRepositoryAdapter(SettingCategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public List<CategoryGottenResult> findAllByTenant(String tenant, YesNo activeFlag) {
        return categoryJpaRepository.findAllByTenant(tenant, activeFlag).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public List<CategoryGottenResult> findAllByTenantIncludeInactive(String tenant) {
        return categoryJpaRepository.findAllByTenantIncludeInactive(tenant).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public Optional<CategoryGottenResult> findByIdAndActiveFlag(Long id, YesNo activeFlag) {
        return categoryJpaRepository.findByIdAndActiveFlag(id, activeFlag)
                .map(this::toResult);
    }

    @Override
    public Optional<CategoryGottenResult> findById(Long id) {
        return categoryJpaRepository.findById(id)
                .map(this::toResult);
    }

    @Override
    public boolean existsByTenantAndCode(String tenant, String code) {
        return categoryJpaRepository.existsByTenantAndCode(tenant, code);
    }

    @Override
    public CategoryCreatedResult save(CreateCategoryCommand command) {
        SettingCategory category = SettingCategory.of(
                command.tenant(),
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
    public void deleteById(Long id) {
        categoryJpaRepository.findById(id).ifPresent(category -> {
            category.delete();
            categoryJpaRepository.save(category);
        });
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
