package com.module.setting.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 下拉選單選項回應資源 (SettingOptionGottenResource)
 * <p>
 * 表現層 Response 載體，嚴格作為純資料載體 (Pure Data Carrier)，禁止包含任何轉換邏輯。
 * 專門供前端下拉式選單使用，提供 name (option.label) 與 code (option.value)。
 * </p>
 *
 * @param code  代碼 (option.value)
 * @param name  名稱 (option.label)
 * @param label 選項顯示標籤 (前端 option.label)
 * @param value 選項值 (前端 option.value)
 */
@Schema(description = "下拉選單選項回應載體")
public record SettingOptionGottenResource(
        @Schema(description = "選項代碼 (值)", example = "FUNCTION")
        String code,

        @Schema(description = "選項名稱 (標籤)", example = "功能清單")
        String name,

        @Schema(description = "前端元件通用 label", example = "功能清單")
        String label,

        @Schema(description = "前端元件通用 value", example = "FUNCTION")
        String value
) {
}
