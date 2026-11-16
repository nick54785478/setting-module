package com.module.setting.application.shared.result;

import com.module.setting.application.shared.vo.YesNo;
import java.time.LocalDateTime;

/**
 * 維度元資料更新結果 DTO (CategoryUpdatedResult)
 */
public record CategoryUpdatedResult(
        Long id,
        String tenant,
        String code,
        String name,
        String description,
        Integer priorityNo,
        YesNo activeFlag,
        String label,
        String value,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryUpdatedResult of(
            Long id,
            String tenant,
            String code,
            String name,
            String description,
            Integer priorityNo,
            YesNo activeFlag,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        String label = (name != null && !name.equals(code)) ? String.format("%s (%s)", name, code) : code;
        return new CategoryUpdatedResult(
                id, tenant, code, name, description, priorityNo, activeFlag, label, code, createdAt, updatedAt
        );
    }
}
