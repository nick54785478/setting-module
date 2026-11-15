package com.module.setting.application.shared.query;

/**
 * 查詢設定物件 (GetSettingQuery)
 * <p>
 * 應用層 Query 物件，封裝多維坐標查詢條件。
 * </p>
 *
 * @param id          設定主鍵 ID (可選)
 * @param tenant      租戶代碼 (可選)
 * @param category    功能維度 (可選)
 * @param subCategory 子分類 (可選)
 * @param code        設定代碼 (可選)
 */
public record GetSettingQuery(
        Long id,
        String tenant,
        String category,
        String subCategory,
        String code
) {
}
