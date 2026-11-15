package com.module.setting.application.port.in;

import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingGottenResult;

/**
 * 分頁查詢設定用例介面 (GetSettingPagedUseCase)
 * <p>
 * 應用層 Inbound Port，定義多條件動態分頁查詢 Setting 資料的業務合約。
 * </p>
 */
public interface GetSettingPagedUseCase {

    /**
     * 執行多條件動態分頁查詢
     *
     * @param query 分頁查詢參數指令
     * @return 包含 SettingGottenResult 的通用分頁結果
     */
    PagedDataGottenResult<SettingGottenResult> getPaged(GetSettingPagedQuery query);
}
