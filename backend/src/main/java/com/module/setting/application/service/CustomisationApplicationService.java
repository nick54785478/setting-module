package com.module.setting.application.service;

import com.module.setting.application.port.in.GetCustomisationUseCase;
import com.module.setting.application.port.in.ResetCustomisationUseCase;
import com.module.setting.application.port.in.SaveCustomisationUseCase;
import com.module.setting.application.port.out.CustomisationRepositoryPort;
import com.module.setting.application.shared.context.UserContextHolder;
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
 * 遵循 Clean Architecture 依賴反轉原則，僅依賴 CustomisationRepositoryPort，不引用 Infrastructure 層之 ORM 或 VO。
 * 欄位清單於資料庫中以「逗號分隔 (CSV)」字串形式儲存，徹底解耦前端 UI 文案 (Label) 與多語系 (i18n)。
 * </p>
 */
@Slf4j
@Service
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

        Optional<String> optionalValue = repositoryPort.findActiveValue(tenant, username, component, type);
        if (optionalValue.isEmpty() || optionalValue.get().isBlank()) {
            log.debug("查無個人化配置或為空, 回傳預設空清單: tenant={}, username={}, component={}, type={}",
                    tenant, username, component, type);
            return Collections.emptyList();
        }

        return Arrays.stream(optionalValue.get().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Transactional
    @Override
    public List<String> saveCustomisation(String component, String type, List<String> fields) {
        String tenant = resolveTenant();
        String username = resolveUsername();

        String csvValue = (fields != null && !fields.isEmpty())
                ? String.join(",", fields)
                : "";

        repositoryPort.save(tenant, username, component, type, csvValue);
        log.info("個人化配置 (CSV) 儲存成功: tenant={}, username={}, component={}, type={}, value={}",
                tenant, username, component, type, csvValue);
        return fields != null ? fields : Collections.emptyList();
    }

    @Transactional
    @Override
    public void resetCustomisation(String component, String type) {
        String tenant = resolveTenant();
        String username = resolveUsername();

        repositoryPort.delete(tenant, username, component, type);
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
