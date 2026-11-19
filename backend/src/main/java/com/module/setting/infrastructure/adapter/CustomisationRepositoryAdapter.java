package com.module.setting.infrastructure.adapter;

import com.module.setting.application.port.out.CustomisationRepositoryPort;
import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.customisation.entity.Customisation;
import com.module.setting.infrastructure.persistence.customisation.repository.CustomisationJpaRepository;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 個人化配置倉儲適配器 (CustomisationRepositoryAdapter)
 * <p>
 * 實作應用層 Outbound Port (CustomisationRepositoryPort)，
 * 封裝 JPA Repository 操作與 Customisation / CustomisationScope 實體物件轉換。
 * </p>
 */
@Component
public class CustomisationRepositoryAdapter implements CustomisationRepositoryPort {

    private final CustomisationJpaRepository jpaRepository;

    public CustomisationRepositoryAdapter(CustomisationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<String> findActiveValue(String tenant, String username, String component, String type) {
        CustomisationScope scope = CustomisationScope.of(tenant, username, component, type);
        return jpaRepository.findByScope(scope)
                .filter(c -> c.getActiveFlag() == YesNo.Y)
                .map(Customisation::getValue);
    }

    @Override
    public void save(String tenant, String username, String component, String type, String value) {
        CustomisationScope scope = CustomisationScope.of(tenant, username, component, type);
        Customisation customisation = jpaRepository.findByScope(scope)
                .map(existing -> {
                    existing.updateValue(value);
                    existing.activate();
                    return existing;
                })
                .orElseGet(() -> Customisation.create(scope, value));
        jpaRepository.save(customisation);
    }

    @Override
    public void delete(String tenant, String username, String component, String type) {
        CustomisationScope scope = CustomisationScope.of(tenant, username, component, type);
        jpaRepository.findByScope(scope).ifPresent(jpaRepository::delete);
    }
}
