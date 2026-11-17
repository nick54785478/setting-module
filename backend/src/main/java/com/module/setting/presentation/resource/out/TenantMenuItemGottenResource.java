package com.module.setting.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 租戶側邊欄選單項目回應資源 (TenantMenuItemGottenResource)
 * <p>
 * 遵循 N+Ved Resource 命名規範，將 category=MENU 之 Setting 實體轉換為側邊欄選單項目 DTO。
 * </p>
 */
@Schema(description = "租戶側邊欄選單項目資料傳輸物件")
public record TenantMenuItemGottenResource(
        @Schema(description = "設定主鍵 ID", example = "101")
        Long id,
        @Schema(description = "歸屬租戶代碼", example = "CW")
        String tenant,
        @Schema(description = "選單所屬群組 (SubCategory)", example = "核心業務配置")
        String group,
        @Schema(description = "選單唯一代碼", example = "MENU_SETTING")
        String code,
        @Schema(description = "選單顯示名稱", example = "功能設定管理")
        String name,
        @Schema(description = "詳細說明", example = "核心業務設定項目增刪改查管理")
        String description,
        @Schema(description = "選單屬性 (JSON 字串，包含 icon, badge, view, url 等擴充)",
                example = "{\"icon\":\"pi pi-sliders-h\",\"badge\":\"核心\",\"view\":\"setting\"}")
        String value,
        @Schema(description = "排序順序號", example = "1")
        Integer priorityNo,
        @Schema(description = "有效性狀態 (Y: 生效, N: 停用)", example = "Y")
        String activeFlag
) {}
