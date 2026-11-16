package com.module.setting.infrastructure.persistence.customisation.repository;

import com.module.setting.infrastructure.persistence.customisation.entity.Customisation;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 個人化配置 Spring Data JPA 倉儲介面
 */
@Repository
public interface CustomisationJpaRepository extends JpaRepository<Customisation, Long> {

    /**
     * 依據業務自然鍵坐標查詢個人化配置
     *
     * @param scope 個人化配置坐標
     * @return 匹配之配置實體
     */
    @Query("SELECT c FROM Customisation c WHERE c.scope = :scope")
    Optional<Customisation> findByScope(@Param("scope") CustomisationScope scope);

    /**
     * 依據租戶、使用者、元件與種類查詢有效配置
     */
    @Query("SELECT c FROM Customisation c WHERE c.scope.tenant = :tenant AND c.scope.username = :username AND c.scope.component = :component AND c.scope.type = :type")
    Optional<Customisation> findByTenantAndUsernameAndComponentAndType(
            @Param("tenant") String tenant,
            @Param("username") String username,
            @Param("component") String component,
            @Param("type") String type
    );
}
