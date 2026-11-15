package com.module.setting.application.shared.query;

/**
 * 分頁查詢設定指令物件 (GetSettingPagedQuery)
 * <p>
 * 應用層 Query 物件，封裝多維篩選條件與分頁參數。
 * </p>
 *
 * @param tenant      租戶識別碼 (可選)
 * @param category    功能大維度 (ROLE, GROUP, FUNCTION 等，可選)
 * @param subCategory 業務子分類 (ADMIN, PM, DASHBOARD 等，可選)
 * @param code        設定代碼 (可選)
 * @param name        設定顯示名稱模糊查詢 (可選)
 * @param activeFlag  有效性旗標 (可選，預設為 "Y")
 * @param page        分頁頁碼 (從 0 開始)
 * @param size        每頁顯示筆數 (預設為 10 或 20)
 */
public record GetSettingPagedQuery(
        String tenant,
        String category,
        String subCategory,
        String code,
        String name,
        String activeFlag,
        int page,
        int size
) {
    public GetSettingPagedQuery {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
    }
}
