package com.module.setting.application.shared.result;

import java.util.List;

/**
 * 通用分頁查詢結果資料傳輸物件 (PagedDataGottenResult)
 * <p>
 * 應用層共用之泛型分頁資料載體 (Pure Data Carrier)，封裝當前頁資料清單、總筆數、總頁數、頁碼與頁面大小。
 * </p>
 *
 * @param <T>           資料項目類型
 * @param content       當前頁資料內容清單
 * @param totalElements 符合條件的總筆數
 * @param totalPages    總頁數
 * @param page          當前頁碼 (從 0 或 1 開始，依呼叫端協議)
 * @param size          每頁筆數
 */
public record PagedDataGottenResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size) {
}