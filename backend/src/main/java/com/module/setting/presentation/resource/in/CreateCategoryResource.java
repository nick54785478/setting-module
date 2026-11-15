package com.module.setting.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "建立維度元資料請求資源")
public record CreateCategoryResource(
        @Schema(description = "所屬租戶 (留空預設為 ALL)", example = "ALL")
        String tenant,

        @NotBlank(message = "維度代碼不得為空")
        @Size(max = 64, message = "維度代碼長度不得超過 64 字元")
        @Schema(description = "維度唯一代碼", requiredMode = Schema.RequiredMode.REQUIRED, example = "FUNCTION")
        String code,

        @NotBlank(message = "維度名稱不得為空")
        @Size(max = 128, message = "維度名稱長度不得超過 128 字元")
        @Schema(description = "維度顯示名稱", requiredMode = Schema.RequiredMode.REQUIRED, example = "功能配置")
        String name,

        @Size(max = 512, message = "描述長度不得超過 512 字元")
        @Schema(description = "維度說明", example = "前端功能與頁面模組配置維度")
        String description,

        @Schema(description = "排序順序號", example = "1")
        Integer priorityNo
) {
}
