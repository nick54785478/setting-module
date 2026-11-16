package com.module.setting.infrastructure.adapter;

import com.module.setting.application.port.out.CustomisationRepositoryPort;
import com.module.setting.infrastructure.persistence.customisation.entity.Customisation;
import com.module.setting.infrastructure.persistence.customisation.repository.CustomisationJpaRepository;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 個人化配置倉儲適配器 (CustomisationRepositoryAdapter)
 */
@Component
public class CustomisationRepositoryAdapter implements CustomisationRepositoryPort {

    private final CustomisationJpaRepository jpaRepository;

    public CustomisationRepositoryAdapter(CustomisationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Customisation> findByScope(CustomisationScope scope) {
        return jpaRepository.findByScope(scope);
    }

    @Override
    public Customisation save(Customisation customisation) {
        return jpaRepository.save(customisation);
    }

    @Override
    public void deleteByScope(CustomisationScope scope) {
        jpaRepository.findByScope(scope).ifPresent(jpaRepository::delete);
    }
}
