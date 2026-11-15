package com.module.setting.application.service;

import com.module.setting.application.port.in.*;
import com.module.setting.application.port.out.SettingRepositoryPort;
import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.command.DeleteSettingCommand;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import com.module.setting.application.shared.template.SettingSeedTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 設定應用層服務 (SettingApplicationService)
 * <p>
 * 實作所有 Inbound Ports，調度 Outbound Port (SettingRepositoryPort) 進行業務流程協調、事務控制與資料初始化。
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
        DeleteSettingUseCase {

    private final SettingRepositoryPort settingRepositoryPort;

    public SettingApplicationService(SettingRepositoryPort settingRepositoryPort) {
        this.settingRepositoryPort = settingRepositoryPort;
    }

    @Override
    public SettingGottenResult create(CreateSettingCommand command) {
        return settingRepositoryPort.save(command);
    }

    @Override
    public SettingGottenResult update(UpdateSettingCommand command) {
        return settingRepositoryPort.update(command);
    }

    @Override
    @Transactional(readOnly = true)
    public SettingGottenResult getById(Long id) {
        return settingRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SettingGottenResult getByScope(String tenant, String code) {
        return settingRepositoryPort.findByScope(tenant, code)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with tenant: " + tenant + ", code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingGottenResult> getByCategory(String tenant, String category) {
        return settingRepositoryPort.findByCategory(tenant, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingGottenResult> getByCategoryAndSubCategory(String tenant, String category, String subCategory) {
        return settingRepositoryPort.findByCategoryAndSubCategory(tenant, category, subCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingGottenResult> getAllByTenant(String tenant) {
        return settingRepositoryPort.findByTenant(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedDataGottenResult<SettingGottenResult> getPaged(GetSettingPagedQuery query) {
        return settingRepositoryPort.findPaged(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingOptionGottenResult> getCategoryOptions(String tenant) {
        return settingRepositoryPort.findCategoryOptions(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingOptionGottenResult> getSubCategoryOptions(String tenant, String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank when retrieving sub-category options.");
        }
        return settingRepositoryPort.findSubCategoryOptions(tenant, category);
    }

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

    @Override
    public void delete(DeleteSettingCommand command) {
        settingRepositoryPort.deleteById(command.id());
    }
}
