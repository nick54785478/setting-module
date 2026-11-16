package com.module.setting.application.port.out;

import com.module.setting.infrastructure.persistence.customisation.entity.Customisation;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;

import java.util.Optional;

/**
 * 個人化配置倉儲輸出埠 (CustomisationRepositoryPort)
 */
public interface CustomisationRepositoryPort {

    /**
     * 依據業務坐標查詢配置
     *
     * @param scope 坐標
     * @return 匹配之配置實體
     */
    Optional<Customisation> findByScope(CustomisationScope scope);

    /**
     * 儲存個人化配置
     *
     * @param customisation 配置實體
     * @return 儲存後實體
     */
    Customisation save(Customisation customisation);

    /**
     * 依據業務坐標刪除配置 (實體刪除)
     *
     * @param scope 坐標
     */
    void deleteByScope(CustomisationScope scope);
}
