package com.module.setting.application.service;

import com.module.setting.application.port.in.GetCustomisationUseCase;
import com.module.setting.application.port.in.ResetCustomisationUseCase;
import com.module.setting.application.port.in.SaveCustomisationUseCase;
import com.module.setting.application.port.out.CustomisationRepositoryPort;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.customisation.entity.Customisation;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 個人化配置應用服務 (CustomisationApplicationService)
 * <p>
 * 實作個人化配置之查詢、保存與重設業務合約。
 * 欄位清單於資料庫中以「逗號分隔 (CSV)」字串形式儲存，徹底解耦前端 UI 文案 (Label) 與多語系 (i18n)。
 * </p>
 */
@Slf4j
@Service
@Transactional
public class CustomisationApplicationService implements
        GetCustomisationUseCase,
        SaveCustomisationUseCase,
        ResetCustomisationUseCase {

    private final CustomisationRepositoryPort repositoryPort;

    public CustomisationApplicationService(CustomisationRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCustomisation(String component, String type) {
        String tenant = resolveTenant();
        String username = resolveUsername();
        CustomisationScope scope = CustomisationScope.of(tenant, username, component, type);

        Optional<Customisation> optional = repositoryPort.findByScope(scope);
        if (optional.isEmpty()) {
            log.debug("查無個人化配置, 回傳預設空清單: tenant={}, username={}, component={}, type={}",
                    tenant, username, component, type);
            return Collections.emptyList();
        }

        Customisation customisation = optional.get();
        if (customisation.getActiveFlag() == YesNo.N) {
            log.debug("個人化配置已停用, 回傳預設空清單: scope={}", scope);
            return Collections.emptyList();
        }

        String rawValue = customisation.getValue();
        if (rawValue == null || rawValue.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public List<String> saveCustomisation(String component, String type, List<String> fields) {
        String tenant = resolveTenant();
        String username = resolveUsername();
        CustomisationScope scope = CustomisationScope.of(tenant, username, component, type);

        String csvValue = (fields != null && !fields.isEmpty())
                ? String.join(",", fields)
                : "";

        Customisation customisation = repositoryPort.findByScope(scope)
                .map(existing -> {
                    existing.updateValue(csvValue);
                    existing.activate();
                    return existing;
                })
                .orElseGet(() -> Customisation.create(scope, csvValue));

        repositoryPort.save(customisation);
        log.info("個人化配置 (CSV) 儲存成功: tenant={}, username={}, component={}, type={}, value={}",
                tenant, username, component, type, csvValue);
        return fields != null ? fields : Collections.emptyList();
    }

    @Override
    public void resetCustomisation(String component, String type) {
        String tenant = resolveTenant();
        String username = resolveUsername();
        CustomisationScope scope = CustomisationScope.of(tenant, username, component, type);

        repositoryPort.deleteByScope(scope);
        log.info("個人化配置已還原為預設: tenant={}, username={}, component={}, type={}",
                tenant, username, component, type);
    }

    private String resolveTenant() {
        String tenant = UserContextHolder.getTenant();
        return (tenant != null && !tenant.isBlank()) ? tenant : "CW";
    }

    private String resolveUsername() {
        String username = UserContextHolder.getUsername();
        return (username != null && !username.isBlank()) ? username : "nickgh.zhang@cw.com";
    }
}
