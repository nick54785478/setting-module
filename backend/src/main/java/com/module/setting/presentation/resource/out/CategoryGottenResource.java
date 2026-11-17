package com.module.setting.presentation.resource.out;

import com.module.setting.application.shared.vo.YesNo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "維度元資料回應資源")
public record CategoryGottenResource(
        @Schema(description = "維度主鍵 ID", example = "1")
        Long id,

        @Schema(description = "所屬租戶", example = "CW")
        String tenant,

        @Schema(description = "維度唯一代碼 (下拉選單 value / code)", example = "FUNCTION")
        String code,

        @Schema(description = "維度名稱", example = "功能配置")
        String name,

        @Schema(description = "維度說明", example = "前端功能與頁面模組配置維度")
        String description,

        @Schema(description = "排序順序號", example = "1")
        Integer priorityNo,

        @Schema(description = "有效性狀態", example = "Y")
        YesNo activeFlag,

        @Schema(description = "下拉選單通用標籤 (option.label)", example = "功能配置 (FUNCTION)")
        String label,

        @Schema(description = "下拉選單通用數值 (option.value)", example = "FUNCTION")
        String value,

        @Schema(description = "建立時間")
        LocalDateTime createdAt,

        @Schema(description = "最後更新時間")
        LocalDateTime updatedAt
) {
}
