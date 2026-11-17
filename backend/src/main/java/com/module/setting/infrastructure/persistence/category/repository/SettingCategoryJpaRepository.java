package com.module.setting.infrastructure.persistence.category.repository;

import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.category.entity.SettingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 維度元資料 Spring Data JPA 倉儲介面 (SettingCategoryJpaRepository)
 */
@Repository
public interface SettingCategoryJpaRepository extends JpaRepository<SettingCategory, Long> {

    /**
     * 查詢指定租戶且符合有效性旗標的所有維度，依 priorityNo 升冪排序
     *
     * @param tenant     租戶代碼 (可選)
     * @param activeFlag 有效性旗標
     * @return 符合條件的維度清單
     */
    @Query("SELECT c FROM SettingCategory c " +
           "WHERE (:tenant IS NULL OR :tenant = '' OR c.tenant = :tenant) " +
           "AND c.activeFlag = :activeFlag " +
           "ORDER BY c.priorityNo ASC")
    List<SettingCategory> findAllByTenant(@Param("tenant") String tenant, @Param("activeFlag") YesNo activeFlag);

    /**
     * 查詢指定租戶之所有維度 (包含已停用)，依 priorityNo 升冪排序
     */
    @Query("SELECT c FROM SettingCategory c " +
           "WHERE (:tenant IS NULL OR :tenant = '' OR c.tenant = :tenant) " +
           "ORDER BY c.priorityNo ASC")
    List<SettingCategory> findAllByTenantIncludeInactive(@Param("tenant") String tenant);

    /**
     * 依據主鍵與有效狀態查詢單一維度
     */
    Optional<SettingCategory> findByIdAndActiveFlag(Long id, YesNo activeFlag);

    /**
     * 檢查指定租戶與代碼是否已存在
     */
    boolean existsByTenantAndCode(String tenant, String code);

    /**
     * 依據租戶與代碼查詢
     */
    Optional<SettingCategory> findByTenantAndCode(String tenant, String code);
}
