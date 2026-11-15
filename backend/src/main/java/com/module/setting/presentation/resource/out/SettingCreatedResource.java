package com.module.setting.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 設定已建立回應資源 (SettingCreatedResource)
 * <p>
 * 表現層 Response 載體，嚴格作為純資料載體 (Pure Data Carrier)，禁止包含任何轉換邏輯。
 * </p>
 *
 * @param id          設定主鍵 ID
 * @param tenant      租戶代碼
 * @param category    功能大維度
 * @param subCategory 業務子分類
 * @param code        設定代碼
 * @param name        設定名稱
 * @param description 設定描述
 * @param value       設定數值 / 標識
 * @param priorityNo  順序號
 * @param activeFlag  是否生效
 */
@Schema(description = "設定建立成功回應載體")
public record SettingCreatedResource(
        @Schema(description = "設定主鍵 ID", example = "101")
        Long id,

        @Schema(description = "租戶識別碼", example = "tenant-01")
        String tenant,

        @Schema(description = "功能大分類 / 維度", example = "FUNCTION")
        String category,

        @Schema(description = "業務子分類 / 模組", example = "DASHBOARD")
        String subCategory,

        @Schema(description = "設定代碼", example = "DASHBOARD_ANALYTICS")
        String code,

        @Schema(description = "設定顯示名稱", example = "儀表板數據分析功能")
        String name,

        @Schema(description = "詳細說明", example = "提供營運統計圖表")
        String description,

        @Schema(description = "設定值 / 路由標識", example = "/analytics")
        String value,

        @Schema(description = "排序優先級", example = "1")
        Integer priorityNo,

        @Schema(description = "有效狀態旗標", example = "Y")
        String activeFlag
) {
}
