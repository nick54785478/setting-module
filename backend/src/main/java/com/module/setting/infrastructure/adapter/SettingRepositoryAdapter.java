package com.module.setting.infrastructure.adapter;

import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.command.ReorderSettingItemCommand;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingCreatedResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.assembler.SettingDtoAssembler;
import com.module.setting.infrastructure.persistence.category.entity.SettingCategory;
import com.module.setting.infrastructure.persistence.category.repository.SettingCategoryJpaRepository;
import com.module.setting.infrastructure.persistence.setting.entity.Setting;
import com.module.setting.infrastructure.persistence.setting.repository.SettingJpaRepository;
import com.module.setting.infrastructure.persistence.setting.vo.SettingProfile;
import com.module.setting.infrastructure.persistence.setting.vo.SettingScope;
import com.module.setting.infrastructure.specification.GetSettingsSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 設定倉儲基礎設施適配器 (SettingRepositoryAdapter)
 * <p>
 * 實作應用層 Outbound Port (SettingRepositoryPort)，作為基礎設施層對外匯聚出口，
 * 協調 JPA Repository、Specification 與 DTO Assembler 完成資料持久化與物件轉換。
 * 自動於各業務查詢與持久化操作中取用 {@link UserContextHolder} 的租戶識別碼。
 * </p>
 */
@Component
public class SettingRepositoryAdapter implements SettingRepositoryPort {

    private final SettingJpaRepository settingJpaRepository;
    private final SettingCategoryJpaRepository categoryJpaRepository;
    private final SettingDtoAssembler settingDtoAssembler;

    public SettingRepositoryAdapter(
            SettingJpaRepository settingJpaRepository,
            SettingCategoryJpaRepository categoryJpaRepository,
            SettingDtoAssembler settingDtoAssembler) {
        this.settingJpaRepository = settingJpaRepository;
        this.categoryJpaRepository = categoryJpaRepository;
        this.settingDtoAssembler = settingDtoAssembler;
    }

    @Override
    public SettingCreatedResult save(CreateSettingCommand command) {
        String tenant = resolveTenant(command.tenant());
        if (tenant == null || tenant.isBlank()) {
            throw new IllegalArgumentException("Tenant is required either via RequestHeader (e.g. X-Tenant-Id) or request body.");
        }

        SettingScope scope = SettingScope.of(
                tenant,
                command.category(),
                command.subCategory(),
                command.code()
        );
        SettingProfile profile = SettingProfile.of(command.name(), command.description());
        Setting setting = Setting.create(
                scope,
                profile,
                command.value(),
                command.priorityNo()
        );

        Setting saved = settingJpaRepository.save(setting);
        return settingDtoAssembler.toCreatedResult(saved);
    }

    @Override
    public SettingUpdatedResult update(UpdateSettingCommand command) {
        Setting setting = settingJpaRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with id: " + command.id()));

        SettingScope newScope = null;
        if (command.category() != null || command.subCategory() != null) {
            String tenant = (setting.getScope() != null && setting.getScope().getTenant() != null)
                    ? setting.getScope().getTenant()
                    : resolveTenant(null);
            String category = command.category() != null ? command.category() : setting.getScope().getCategory();
            String subCategory = command.subCategory() != null ? command.subCategory() : setting.getScope().getSubCategory();
            newScope = SettingScope.of(tenant, category, subCategory, setting.getScope().getCode());
        }

        SettingProfile profile = SettingProfile.of(command.name(), command.description());
        setting.update(
                newScope,
                profile,
                command.value(),
                command.priorityNo(),
                command.activeFlag()
        );

        Setting updated = settingJpaRepository.save(setting);
        return settingDtoAssembler.toUpdatedResult(updated);
    }

    @Override
    public boolean exists(String tenant, String category, String code) {
        String resolvedTenant = resolveTenant(tenant);
        return settingJpaRepository.existsByScopeTenantAndScopeCategoryAndScopeCode(resolvedTenant, category, code);
    }

    @Override
    public Optional<SettingGottenResult> findById(Long id) {
        return settingJpaRepository.findByIdAndActiveFlag(id, YesNo.Y)
                .map(settingDtoAssembler::toResult);
    }

    @Override
    public Optional<SettingGottenResult> findByScope(String tenant, String code) {
        String resolvedTenant = resolveTenant(tenant);
        return settingJpaRepository.findByScopeTenantAndScopeCodeAndActiveFlag(resolvedTenant, code, YesNo.Y)
                .map(settingDtoAssembler::toResult);
    }

    @Override
    public List<SettingGottenResult> findByCategory(String tenant, String category) {
        String resolvedTenant = resolveTenant(tenant);
        return settingJpaRepository.findAllByScopeTenantAndScopeCategoryAndActiveFlagOrderByPriorityNoAsc(
                        resolvedTenant, category, YesNo.Y)
                .stream()
                .map(settingDtoAssembler::toResult)
                .toList();
    }

    @Override
    public List<SettingGottenResult> findByCategoryAndSubCategory(String tenant, String category, String subCategory) {
        String resolvedTenant = resolveTenant(tenant);
        return settingJpaRepository.findAllByScopeTenantAndScopeCategoryAndScopeSubCategoryAndActiveFlagOrderByPriorityNoAsc(
                        resolvedTenant, category, subCategory, YesNo.Y)
                .stream()
                .map(settingDtoAssembler::toResult)
                .toList();
    }

    @Override
    public List<SettingGottenResult> findByTenant(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        return settingJpaRepository.findAllByScopeTenantAndActiveFlagOrderByPriorityNoAsc(resolvedTenant, YesNo.Y)
                .stream()
                .map(settingDtoAssembler::toResult)
                .toList();
    }

    @Override
    public PagedDataGottenResult<SettingGottenResult> findPaged(GetSettingPagedQuery query) {
        Specification<Setting> spec = GetSettingsSpecification.fromQuery(query).toSpecification();
        Sort sort = Sort.by(Sort.Order.asc("priorityNo"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);
        Page<Setting> page = settingJpaRepository.findAll(spec, pageable);

        List<SettingGottenResult> content = page.getContent().stream()
                .map(settingDtoAssembler::toResult)
                .toList();

        return new PagedDataGottenResult<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    @Override
    public List<SettingOptionGottenResult> findCategoryOptions(String tenant) {
        String resolvedTenant = resolveTenant(tenant);
        // 直接從獨立的 SettingCategory 字典倉儲取得大維度清單
        List<SettingCategory> categories = categoryJpaRepository.findAllByTenant(resolvedTenant, YesNo.Y);
        return categories.stream()
                .map(c -> {
                    String label = String.format("%s (%s)", c.getName(), c.getCode());
                    return new SettingOptionGottenResult(c.getCode(), c.getName(), label, c.getCode());
                })
                .toList();
    }

    @Override
    public List<SettingOptionGottenResult> findSubCategoryOptions(String tenant, String category) {
        String resolvedTenant = resolveTenant(tenant);
        // 直接查詢該分類下所有真實存在且不重複的子模組/子分類代碼清單
        List<String> distinctSubs = settingJpaRepository.findDistinctSubCategoriesByCategory(resolvedTenant, category, YesNo.Y);
        return distinctSubs.stream()
                .map(sub -> new SettingOptionGottenResult(sub, sub, sub, sub))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        settingJpaRepository.findById(id).ifPresent(setting -> {
            setting.delete();
            settingJpaRepository.save(setting);
        });
    }

    @Override
    public List<SettingGottenResult> reorder(List<ReorderSettingItemCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }

        List<Setting> updatedSettings = new ArrayList<>();
        for (int i = 0; i < commands.size(); i++) {
            ReorderSettingItemCommand cmd = commands.get(i);
            if (cmd == null || cmd.id() == null) {
                continue;
            }
            int priority = (cmd.priorityNo() != null && cmd.priorityNo() > 0)
                    ? cmd.priorityNo()
                    : (i + 1);

            Optional<Setting> optionalSetting = settingJpaRepository.findById(cmd.id());
            if (optionalSetting.isPresent()) {
                Setting setting = optionalSetting.get();
                setting.updatePriority(priority);
                updatedSettings.add(settingJpaRepository.save(setting));
            }
        }

        return updatedSettings.stream()
                .sorted(Comparator.comparing(Setting::getPriorityNo)
                        .thenComparing(Setting::getId))
                .map(settingDtoAssembler::toResult)
                .toList();
    }

    /**
     * 解析租戶代碼：優先採用顯式傳入的 tenant，若無則從 UserContextHolder 讀取
     *
     * @param tenant 顯式傳入的租戶代碼
     * @return 解析後的租戶代碼，可能為 null
     */
    private String resolveTenant(String tenant) {
        if (tenant != null && !tenant.isBlank()) {
            return tenant.trim();
        }
        return UserContextHolder.getTenant();
    }
}
