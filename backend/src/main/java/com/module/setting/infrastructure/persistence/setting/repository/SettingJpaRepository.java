package com.module.setting.infrastructure.persistence.setting.repository;

import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.setting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 設定 Spring Data JPA 倉儲介面 (SettingJpaRepository)
 * <p>
 * 提供對 setting 資料表的底層持久化存取操作，支援 Function Configurable 多維度條件查詢、動態 Specification 分頁
 * 以及元資料驅動 (META) 之 Category / SubCategory 選項查詢。
 * </p>
 */
@Repository
public interface SettingJpaRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {

    /**
     * 依據主鍵與有效狀態查詢設定
     *
     * @param id         主鍵 ID
     * @param activeFlag 有效狀態
     * @return 符合條件的設定可選物件
     */
    Optional<Setting> findByIdAndActiveFlag(Long id, YesNo activeFlag);

    /**
     * 依據租戶與代碼查詢單一有效設定
     *
     * @param tenant     租戶代碼
     * @param code       設定代碼
     * @param activeFlag 有效狀態
     * @return 符合條件的設定可選物件
     */
    Optional<Setting> findByScopeTenantAndScopeCodeAndActiveFlag(String tenant, String code, YesNo activeFlag);

    /**
     * 檢查指定租戶、大維度與代碼之設定是否存在
     *
     * @param tenant   租戶代碼
     * @param category 業務功能維度
     * @param code     設定代碼
     * @return 若存在回傳 true，否則回傳 false
     */
    boolean existsByScopeTenantAndScopeCategoryAndScopeCode(String tenant, String category, String code);

    /**
     * 依據租戶與大分類 (Category) 查詢所有有效設定 (依順序號升冪排序)
     *
     * @param tenant     租戶代碼
     * @param category   業務功能維度 (例如: FUNCTION, ROLE)
     * @param activeFlag 有效狀態
     * @return 符合條件的設定清單
     */
    List<Setting> findAllByScopeTenantAndScopeCategoryAndActiveFlagOrderByPriorityNoAsc(
            String tenant, String category, YesNo activeFlag);

    /**
     * 依據租戶、大分類與子分類 (SubCategory) 查詢所有有效設定 (依順序號升冪排序)
     *
     * @param tenant      租戶代碼
     * @param category    業務功能維度
     * @param subCategory 業務子分類 (例如: DASHBOARD, USER)
     * @param activeFlag  有效狀態
     * @return 符合條件的設定清單
     */
    List<Setting> findAllByScopeTenantAndScopeCategoryAndScopeSubCategoryAndActiveFlagOrderByPriorityNoAsc(
            String tenant, String category, String subCategory, YesNo activeFlag);

    /**
     * 依據租戶與有效狀態查詢所有設定 (依順序號升冪排序)
     *
     * @param tenant     租戶代碼
     * @param activeFlag 有效狀態
     * @return 符合條件的設定清單
     */
    List<Setting> findAllByScopeTenantAndActiveFlagOrderByPriorityNoAsc(String tenant, YesNo activeFlag);

    /**
     * 依據有效狀態查詢所有設定
     *
     * @param activeFlag 有效狀態
     * @return 所有符合有效狀態的設定清單
     */
    List<Setting> findAllByActiveFlag(YesNo activeFlag);

    /**
     * 查詢元資料層 (META) 註冊之 Category 定義清單 (Category = 'META' 且 SubCategory = 'CATEGORY')
     * 支援指定租戶或全域 'ALL' 定義，依 priorityNo 升冪排序
     *
     * @param tenant     租戶代碼 (可選)
     * @param activeFlag 有效狀態
     * @return Category 元資料定義項目清單
     */
    @Query("SELECT s FROM Setting s " +
           "WHERE (:tenant IS NULL OR :tenant = '' OR s.scope.tenant = :tenant OR s.scope.tenant = 'ALL') " +
           "AND s.scope.category = 'META' " +
           "AND s.scope.subCategory = 'CATEGORY' " +
           "AND s.activeFlag = :activeFlag " +
           "ORDER BY s.priorityNo ASC")
    List<Setting> findCategoryMetadataDefinitions(@Param("tenant") String tenant, @Param("activeFlag") YesNo activeFlag);

    /**
     * 查詢指定 Category 下所有不重複且非空之 SubCategory 字串清單
     *
     * @param tenant     租戶代碼 (可選)
     * @param category   業務功能大分類
     * @param activeFlag 有效狀態
     * @return 不重複的 SubCategory 字串清單
     */
    @Query("SELECT DISTINCT s.scope.subCategory FROM Setting s " +
           "WHERE (:tenant IS NULL OR :tenant = '' OR s.scope.tenant = :tenant OR s.scope.tenant = 'ALL') " +
           "AND s.scope.category = :category " +
           "AND s.scope.subCategory IS NOT NULL " +
           "AND s.scope.subCategory <> '' " +
           "AND s.activeFlag = :activeFlag " +
           "ORDER BY s.scope.subCategory ASC")
    List<String> findDistinctSubCategoriesByCategory(@Param("tenant") String tenant,
                                                    @Param("category") String category,
                                                    @Param("activeFlag") YesNo activeFlag);

    /**
     * 查詢所有在 setting 資料表中實際存在且非空之 Category 清單 (包含 META 及自定義分類)
     *
     * @param tenant     租戶代碼 (可選)
     * @param activeFlag 有效狀態
     * @return 實際存在於資料表中的 Category 清單
     */
    @Query("SELECT DISTINCT s.scope.category FROM Setting s " +
           "WHERE (:tenant IS NULL OR :tenant = '' OR s.scope.tenant = :tenant OR s.scope.tenant = 'ALL') " +
           "AND s.scope.category IS NOT NULL AND s.scope.category <> '' " +
           "AND s.activeFlag = :activeFlag " +
           "ORDER BY s.scope.category ASC")
    List<String> findAllExistingCategories(@Param("tenant") String tenant, @Param("activeFlag") YesNo activeFlag);

    /**
     * 查詢所有不重複之業務功能維度 (Category) 作為後備相容
     *
     * @param tenant     租戶代碼 (可選)
     * @param activeFlag 有效狀態
     * @return 不重複的 Category 字串清單
     */
    @Query("SELECT DISTINCT s.scope.category FROM Setting s " +
           "WHERE (:tenant IS NULL OR :tenant = '' OR s.scope.tenant = :tenant OR s.scope.tenant = 'ALL') " +
           "AND s.scope.category <> 'META' " +
           "AND s.activeFlag = :activeFlag " +
           "ORDER BY s.scope.category ASC")
    List<String> findDistinctCategories(@Param("tenant") String tenant, @Param("activeFlag") YesNo activeFlag);
}
