package com.module.setting.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 租戶支援系統語系選項回應資源 (TenantLocaleOptionGottenResource)
 * <p>
 * 遵循 N+Ved Resource 命名規範，封裝租戶支援之系統語系查詢結果。
 * </p>
 */
@Schema(description = "租戶支援系統語系選項資源")
public record TenantLocaleOptionGottenResource(
        @Schema(description = "設定主鍵 ID", example = "1")
        Long id,

        @Schema(description = "租戶識別代碼", example = "CW")
        String tenant,

        @Schema(description = "語系唯一代碼 (如: zh-TW, en-US, ja-JP)", example = "zh-TW")
        String code,

        @Schema(description = "語系顯示名稱", example = "繁體中文")
        String name,

        @Schema(description = "詳細說明或描述", example = "繁體中文 (台灣)")
        String description,

        @Schema(description = "是否為該租戶預設語系", example = "true")
        boolean isDefault,

        @Schema(description = "顯示順序號", example = "1")
        Integer priorityNo,

        @Schema(description = "有效性狀態 (Y: 有效, N: 停用)", example = "Y")
        String activeFlag
) {
}
