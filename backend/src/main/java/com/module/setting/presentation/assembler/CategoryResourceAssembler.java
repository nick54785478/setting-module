package com.module.setting.presentation.assembler;

import com.module.setting.application.shared.result.CategoryCreatedResult;
import com.module.setting.application.shared.result.CategoryGottenResult;
import com.module.setting.application.shared.result.CategoryUpdatedResult;
import com.module.setting.presentation.resource.out.CategoryCreatedResource;
import com.module.setting.presentation.resource.out.CategoryGottenResource;
import com.module.setting.presentation.resource.out.CategoryUpdatedResource;
import org.springframework.stereotype.Component;

/**
 * 維度元資料表現層資源組裝器 (CategoryResourceAssembler)
 * <p>
 * 專門負責維度領域 Result DTO 轉 Response Resource，確保 Resource 物件維持純資料載體特性。
 * </p>
 */
@Component
public class CategoryResourceAssembler {

    /**
     * 將維度建立結果 DTO 轉換為建立成功回應資源
     *
     * @param result 維度建立結果 DTO
     * @return 建立成功回應資源
     */
    public CategoryCreatedResource toCreatedResource(CategoryCreatedResult result) {
        if (result == null) {
            return null;
        }
        return new CategoryCreatedResource(
                result.id(),
                result.tenant(),
                result.code(),
                result.name(),
                result.description(),
                result.priorityNo(),
                result.activeFlag(),
                result.label(),
                result.value(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    /**
     * 將維度更新結果 DTO 轉換為更新成功回應資源
     *
     * @param result 維度更新結果 DTO
     * @return 更新成功回應資源
     */
    public CategoryUpdatedResource toUpdatedResource(CategoryUpdatedResult result) {
        if (result == null) {
            return null;
        }
        return new CategoryUpdatedResource(
                result.id(),
                result.tenant(),
                result.code(),
                result.name(),
                result.description(),
                result.priorityNo(),
                result.activeFlag(),
                result.label(),
                result.value(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    /**
     * 將維度查詢結果 DTO 轉換為查詢回應資源
     *
     * @param result 維度查詢結果 DTO
     * @return 查詢回應資源
     */
    public CategoryGottenResource toGottenResource(CategoryGottenResult result) {
        if (result == null) {
            return null;
        }
        return new CategoryGottenResource(
                result.id(),
                result.tenant(),
                result.code(),
                result.name(),
                result.description(),
                result.priorityNo(),
                result.activeFlag(),
                result.label(),
                result.value(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
