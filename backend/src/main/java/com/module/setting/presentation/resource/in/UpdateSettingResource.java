package com.module.setting.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 更新設定請求資源 (UpdateSettingResource)
 * <p>
 * 表現層 Request 載體，嚴格作為純資料載體 (Pure Data Carrier)，禁止包含任何轉換邏輯。
 * </p>
 *
 * @param category    功能大分類 (可選)
 * @param subCategory 業務子分類 (可選)
 * @param name        設定名稱
 * @param description 設定描述
 * @param value       設定數值 / 標識
 * @param priorityNo  順序號 (從 1 開始)
 * @param activeFlag  是否生效 (Y / N)
 */
@Schema(description = "更新設定請求載體")
public record UpdateSettingResource(
        @Schema(description = "功能大分類 / 維度 (例如: FUNCTION, ROLE, GROUP)", example = "FUNCTION")
        @Size(max = 50, message = "category cannot exceed 50 characters")
        String category,

        @Schema(description = "業務子分類 / 模組 (例如: DASHBOARD, USER, SYSTEM)", example = "DASHBOARD")
        @Size(max = 50, message = "subCategory cannot exceed 50 characters")
        String subCategory,

        @Schema(description = "設定顯示名稱", example = "儀表板數據分析功能 (已更新)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name cannot exceed 100 characters")
        String name,

        @Schema(description = "設定詳細描述", example = "提供營運統計圖表與實時監控指標")
        @Size(max = 255, message = "description cannot exceed 255 characters")
        String description,

        @Schema(description = "設定數值 / 路由標識 / 配置值", example = "/analytics/v2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "value is required")
        String value,

        @Schema(description = "排序優先級 (從 1 開始遞增)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "priorityNo is required")
        @Positive(message = "priorityNo must be greater than 0")
        Integer priorityNo,

        @Schema(description = "是否有效旗標 (Y: 有效, N: 停用/邏輯刪除)", example = "Y", allowableValues = {"Y", "N"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "activeFlag is required")
        @Pattern(regexp = "^[YN]$", message = "activeFlag must be 'Y' or 'N'")
        String activeFlag
) {
}
