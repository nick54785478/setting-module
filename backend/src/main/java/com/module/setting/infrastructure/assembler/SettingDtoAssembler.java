package com.module.setting.infrastructure.assembler;

import com.module.setting.application.shared.result.SettingCreatedResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import com.module.setting.infrastructure.persistence.setting.entity.Setting;
import org.springframework.stereotype.Component;

/**
 * 設定 DTO 組裝器 (SettingDtoAssembler)
 * <p>
 * 負責將 Setting 持久化實體轉換為應用層之 Result DTOs (Created / Updated / Gotten)，
 * 確保實體與純資料載體間的單一職責與解耦。
 * </p>
 */
@Component
public class SettingDtoAssembler {

    /**
     * 將 Setting 實體轉換為 SettingGottenResult DTO
     *
     * @param entity Setting 持久化實體
     * @return 轉換後的 SettingGottenResult DTO，若實體為 null 則回傳 null
     */
    public SettingGottenResult toResult(Setting entity) {
        if (entity == null) {
            return null;
        }

        String tenant = entity.getScope() != null ? entity.getScope().getTenant() : null;
        String category = entity.getScope() != null ? entity.getScope().getCategory() : null;
        String subCategory = entity.getScope() != null ? entity.getScope().getSubCategory() : null;
        String code = entity.getScope() != null ? entity.getScope().getCode() : null;
        String name = entity.getProfile() != null ? entity.getProfile().getName() : null;
        String description = entity.getProfile() != null ? entity.getProfile().getDescription() : null;
        String activeFlag = entity.getActiveFlag() != null ? entity.getActiveFlag().name() : null;

        return new SettingGottenResult(
                entity.getId(),
                tenant,
                category,
                subCategory,
                code,
                name,
                description,
                entity.getValue(),
                entity.getPriorityNo(),
                activeFlag
        );
    }

    /**
     * 將 Setting 實體轉換為 SettingCreatedResult DTO
     *
     * @param entity Setting 持久化實體
     * @return 轉換後的 SettingCreatedResult DTO，若實體為 null 則回傳 null
     */
    public SettingCreatedResult toCreatedResult(Setting entity) {
        if (entity == null) {
            return null;
        }

        String tenant = entity.getScope() != null ? entity.getScope().getTenant() : null;
        String category = entity.getScope() != null ? entity.getScope().getCategory() : null;
        String subCategory = entity.getScope() != null ? entity.getScope().getSubCategory() : null;
        String code = entity.getScope() != null ? entity.getScope().getCode() : null;
        String name = entity.getProfile() != null ? entity.getProfile().getName() : null;
        String description = entity.getProfile() != null ? entity.getProfile().getDescription() : null;
        String activeFlag = entity.getActiveFlag() != null ? entity.getActiveFlag().name() : null;

        return new SettingCreatedResult(
                entity.getId(),
                tenant,
                category,
                subCategory,
                code,
                name,
                description,
                entity.getValue(),
                entity.getPriorityNo(),
                activeFlag
        );
    }

    /**
     * 將 Setting 實體轉換為 SettingUpdatedResult DTO
     *
     * @param entity Setting 持久化實體
     * @return 轉換後的 SettingUpdatedResult DTO，若實體為 null 則回傳 null
     */
    public SettingUpdatedResult toUpdatedResult(Setting entity) {
        if (entity == null) {
            return null;
        }

        String tenant = entity.getScope() != null ? entity.getScope().getTenant() : null;
        String category = entity.getScope() != null ? entity.getScope().getCategory() : null;
        String subCategory = entity.getScope() != null ? entity.getScope().getSubCategory() : null;
        String code = entity.getScope() != null ? entity.getScope().getCode() : null;
        String name = entity.getProfile() != null ? entity.getProfile().getName() : null;
        String description = entity.getProfile() != null ? entity.getProfile().getDescription() : null;
        String activeFlag = entity.getActiveFlag() != null ? entity.getActiveFlag().name() : null;

        return new SettingUpdatedResult(
                entity.getId(),
                tenant,
                category,
                subCategory,
                code,
                name,
                description,
                entity.getValue(),
                entity.getPriorityNo(),
                activeFlag
        );
    }
}
