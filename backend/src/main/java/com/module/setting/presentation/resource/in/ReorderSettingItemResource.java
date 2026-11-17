package com.module.setting.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 排序設定項目請求資源 (ReorderSettingItemResource)
 *
 * @param id         設定主鍵 ID
 * @param priorityNo 欲設定之順序號 (若未指定，後端將依傳入陣列索引自動給定 1, 2, 3...)
 */
@Schema(description = "排序設定項目請求資源")
public record ReorderSettingItemResource(
        @NotNull(message = "設定 ID 不得為空")
        @Schema(description = "設定主鍵 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "9")
        Long id,

        @Schema(description = "順序號 (若未指定，後端將依陣列順序自動遞增給定)", example = "1")
        Integer priorityNo
) {
}
