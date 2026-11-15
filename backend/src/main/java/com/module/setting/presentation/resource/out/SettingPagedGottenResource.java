package com.module.setting.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 設定分頁查詢回應資源 (SettingPagedGottenResource)
 * <p>
 * 表現層 Response 載體，嚴格作為純資料載體 (Pure Data Carrier)，禁止包含任何轉換邏輯。
 * </p>
 *
 * @param content       分頁項目清單
 * @param totalElements 總筆數
 * @param totalPages    總頁數
 * @param page          當前頁碼
 * @param size          每頁筆數
 */
@Schema(description = "設定分頁查詢回應載體")
public record SettingPagedGottenResource(
        @Schema(description = "當前分頁設定項目清單")
        List<SettingGottenResource> content,

        @Schema(description = "符合查詢條件之總資料筆數", example = "42")
        long totalElements,

        @Schema(description = "總頁數", example = "5")
        int totalPages,

        @Schema(description = "當前頁碼 (從 0 開始)", example = "0")
        int page,

        @Schema(description = "每頁資料筆數", example = "10")
        int size
) {
}
