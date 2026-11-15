package com.module.setting.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 建立設定請求資源 (CreateSettingResource)
 * <p>
 * 表現層 Request 載體，嚴格作為純資料載體 (Pure Data Carrier)，禁止包含任何轉換邏輯。
 * 租戶標識可直接由 Request Header (X-Tenant-Id) 帶入，此處為可選欄位。
 * </p>
 *
 * @param tenant      租戶代碼 (可選，若 Header 已帶入可不填)
 * @param category    業務功能維度 (例如: ROLE, GROUP, FUNCTION)
 * @param subCategory 業務子分類 (例如: ADMIN, PM, DASHBOARD)
 * @param code        設定唯一識別碼 (例如: DASHBOARD_FUNCTION)
 * @param name        設定顯示名稱
 * @param description 設定詳細描述
 * @param value       設定數值 / 標識
 * @param priorityNo  順序號 (從 1 開始)
 */
@Schema(description = "建立設定請求載體")
public record CreateSettingResource(
        @Schema(description = "租戶識別碼 (可選，未傳入將自動採用 X-Tenant-Id 標頭值)", example = "tenant-01")
        @Size(max = 50, message = "tenant cannot exceed 50 characters")
        String tenant,

        @Schema(description = "功能大分類 / 維度 (例如: FUNCTION, ROLE, GROUP)", example = "FUNCTION", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "category is required")
        @Size(max = 50, message = "category cannot exceed 50 characters")
        String category,

        @Schema(description = "業務子分類 / 模組 (例如: DASHBOARD, USER, SYSTEM)", example = "DASHBOARD")
        @Size(max = 50, message = "subCategory cannot exceed 50 characters")
        String subCategory,

        @Schema(description = "設定唯一識別代碼", example = "DASHBOARD_ANALYTICS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "code is required")
        @Size(max = 50, message = "code cannot exceed 50 characters")
        String code,

        @Schema(description = "設定顯示名稱", example = "儀表板數據分析功能", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name cannot exceed 100 characters")
        String name,

        @Schema(description = "設定詳細描述", example = "提供營運統計圖表與實時監控指標")
        @Size(max = 255, message = "description cannot exceed 255 characters")
        String description,

        @Schema(description = "設定數值 / 路由標識 / 配置值", example = "/analytics", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "value is required")
        String value,

        @Schema(description = "排序優先級 (從 1 開始遞增)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "priorityNo is required")
        @Positive(message = "priorityNo must be greater than 0")
        Integer priorityNo
) {
}
