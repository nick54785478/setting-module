package com.module.setting.infrastructure.specification;

import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.setting.entity.Setting;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * 設定分頁動態查詢 Specification (GetSettingsSpecification)
 * <p>
 * 基礎設施層持久化規範物件，封裝 JPA Criteria 條件組裝邏輯，支援租戶、維度、子分類、名稱與啟用狀態動態過濾。
 * 租戶標識支援由查詢參數傳入或自動從應用層 {@link UserContextHolder} 取用。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetSettingsSpecification {

    private String tenant;
    private String category;
    private String subCategory;
    private String code;
    private String name;
    private String activeFlag;

    /**
     * 從應用層 Query 物件建立 Specification
     * <p>
     * 若 Query 物件中未指定 tenant，將自動從 UserContextHolder 中取得當前執行緒之租戶標識。
     * </p>
     *
     * @param query 分頁查詢參數物件
     * @return GetSettingsSpecification 實例
     */
    public static GetSettingsSpecification fromQuery(GetSettingPagedQuery query) {
        String resolvedTenant = (query.tenant() != null && !query.tenant().isBlank())
                ? query.tenant()
                : UserContextHolder.getTenant();

        return new GetSettingsSpecification(
                resolvedTenant,
                query.category(),
                query.subCategory(),
                query.code(),
                query.name(),
                query.activeFlag()
        );
    }

    /**
     * 轉換為 Spring Data JPA Specification
     *
     * @return JPA Specification<Setting>
     */
    public Specification<Setting> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String targetTenant = (tenant != null && !tenant.isBlank())
                    ? tenant.trim()
                    : UserContextHolder.getTenant();

            if (targetTenant != null && !targetTenant.isBlank()) {
                predicates.add(cb.equal(root.get("scope").get("tenant"), targetTenant));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("scope").get("category"), category.trim()));
            }

            if (subCategory != null && !subCategory.isBlank()) {
                predicates.add(cb.like(root.get("scope").get("subCategory"), "%" + subCategory.trim() + "%"));
            }

            if (code != null && !code.isBlank()) {
                predicates.add(cb.equal(root.get("scope").get("code"), code.trim()));
            }

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(root.get("profile").get("name"), "%" + name.trim() + "%"));
            }

            if (activeFlag != null && !activeFlag.isBlank()) {
                try {
                    YesNo flag = YesNo.valueOf(activeFlag.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("activeFlag"), flag));
                } catch (IllegalArgumentException e) {
                    predicates.add(cb.equal(root.get("activeFlag"), YesNo.Y));
                }
            } else {
                predicates.add(cb.equal(root.get("activeFlag"), YesNo.Y));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
